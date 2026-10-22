package com.stock4event.backend.service;

import com.stock4event.backend.config.EmailProperties;
import com.stock4event.backend.entity.EmailBoasVindas;
import com.stock4event.backend.entity.EmailBoasVindas.Status;
import com.stock4event.backend.entity.EmailBoasVindas.Tipo;
import com.stock4event.backend.entity.Usuario;
import com.stock4event.backend.repository.EmailBoasVindasRepository;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * E-mail de boas-vindas enviado após o cadastro de uma nova conta.
 *
 * <p>Fluxo: {@link #registrar} grava o pedido de envio na mesma transação do cadastro; depois
 * do commit, {@link #aoConfirmarCadastro} faz a primeira tentativa em segundo plano. Se o
 * cadastro falhar ou for desfeito, o registro some junto e nada é enviado. Falhas temporárias
 * ficam pendentes e são refeitas pelo {@link BoasVindasReprocessador}, com espera crescente e
 * limite de tentativas. Cada tentativa reserva o registro com um UPDATE condicional, então duas
 * execuções simultâneas nunca enviam a mesma mensagem.</p>
 *
 * <p>Esta mensagem não confirma que o endereço pertence ao usuário: o projeto ainda não tem
 * verificação de e-mail, e nada aqui marca o endereço como verificado.</p>
 */
@Service
public class BoasVindasEmailService {

    private static final Logger log = LoggerFactory.getLogger(BoasVindasEmailService.class);
    private static final int LOTE_REPROCESSAMENTO = 50;

    /** Publicado no cadastro; tratado só depois que a transação é confirmada no banco. */
    public record BoasVindasRegistrado(Long emailId) {}

    private final EmailBoasVindasRepository repository;
    private final BoasVindasTemplate template;
    private final EmailProperties propriedades;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final ApplicationEventPublisher eventos;

    public BoasVindasEmailService(EmailBoasVindasRepository repository,
                                  BoasVindasTemplate template,
                                  EmailProperties propriedades,
                                  ObjectProvider<JavaMailSender> mailSender,
                                  ApplicationEventPublisher eventos) {
        this.repository = repository;
        this.template = template;
        this.propriedades = propriedades;
        this.mailSender = mailSender;
        this.eventos = eventos;
    }

    /**
     * Agenda o e-mail da conta recém-salva. Deve ser chamado dentro da transação do cadastro.
     * Não faz nada com o envio desabilitado, com perfil sem versão de e-mail ou se a conta já
     * tiver um registro.
     */
    public void registrar(Usuario usuario) {
        if (!propriedades.isHabilitado()) {
            return;
        }
        Optional<Tipo> tipo = tipoPorPerfil(usuario.getPerfilUso());
        if (tipo.isEmpty()) {
            log.warn("Boas-vindas não enviado: perfil '{}' não reconhecido (usuário {}).",
                    usuario.getPerfilUso(), usuario.getId());
            return;
        }
        if (repository.findByUsuarioId(usuario.getId()).isPresent()) {
            return;
        }

        LocalDateTime agora = LocalDateTime.now();
        EmailBoasVindas email = new EmailBoasVindas();
        email.setUsuarioId(usuario.getId());
        email.setDestinatario(usuario.getEmail());
        email.setNomeDestinatario(usuario.getNome());
        email.setTipo(tipo.get());
        email.setStatus(Status.PENDENTE);
        email.setTentativas(0);
        email.setProximaTentativaEm(agora);
        email.setCriadoEm(agora);
        email.setAtualizadoEm(agora);
        repository.save(email);

        eventos.publishEvent(new BoasVindasRegistrado(email.getId()));
    }

    /** Versão do e-mail pelo perfil salvo. "Cliente e fornecedor" recebe a versão de fornecedor. */
    public static Optional<Tipo> tipoPorPerfil(String perfilUso) {
        if (perfilUso == null) return Optional.empty();
        return switch (perfilUso) {
            case "Cliente" -> Optional.of(Tipo.CLIENTE);
            case "Fornecedor", "ClienteFornecedor" -> Optional.of(Tipo.FORNECEDOR);
            default -> Optional.empty();
        };
    }

    /** Primeira tentativa, fora da requisição do cadastro e só depois do commit. */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoConfirmarCadastro(BoasVindasRegistrado evento) {
        processar(evento.emailId());
    }

    /**
     * Faz uma tentativa de envio, se o registro estiver pendente e no prazo.
     * @return true se a mensagem foi aceita pelo servidor SMTP nesta chamada
     */
    public boolean processar(Long emailId) {
        LocalDateTime agora = LocalDateTime.now();
        if (repository.reservarParaEnvio(emailId, agora, Status.PENDENTE, Status.ENVIANDO) == 0) {
            return false; // já enviado, em andamento em outra execução ou fora do prazo
        }
        EmailBoasVindas email = repository.findById(emailId).orElseThrow();

        try {
            enviar(email);
        } catch (Exception e) {
            registrarFalha(email, e);
            return false;
        }

        email.setStatus(Status.ENVIADO);
        email.setEnviadoEm(LocalDateTime.now());
        email.setAtualizadoEm(email.getEnviadoEm());
        email.setUltimoErro(null);
        repository.save(email);
        log.info("Boas-vindas ({}) enviado para {} (usuário {}, tentativa {}).",
                email.getTipo(), mascarar(email.getDestinatario()), email.getUsuarioId(), email.getTentativas());
        return true;
    }

    /** Rodada do agendador: encerra tentativas travadas e refaz as pendentes que já venceram. */
    public int reprocessarPendentes() {
        LocalDateTime agora = LocalDateTime.now();
        int travados = repository.marcarTravadosComoIndeterminados(
                agora.minusMinutes(propriedades.getLimiteTentativaTravadaMinutos()), agora,
                Status.ENVIANDO, Status.INDETERMINADO);
        if (travados > 0) {
            log.warn("{} e-mail(s) de boas-vindas ficaram sem confirmação do servidor e não serão reenviados "
                    + "automaticamente (status INDETERMINADO).", travados);
        }

        List<Long> prontos = repository.idsProntosParaEnvio(agora, Status.PENDENTE,
                PageRequest.of(0, LOTE_REPROCESSAMENTO));
        int enviados = 0;
        for (Long id : prontos) {
            if (processar(id)) enviados++;
        }
        return enviados;
    }

    private void enviar(EmailBoasVindas email) throws Exception {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            throw new IllegalStateException("Servidor SMTP não configurado (SPRING_MAIL_HOST).");
        }
        BoasVindasTemplate.EmailRenderizado conteudo =
                template.renderizar(email.getTipo(), email.getNomeDestinatario());

        MimeMessage mensagem = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mensagem, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
        helper.setFrom(new InternetAddress(propriedades.getRemetente().trim(), true));
        helper.setTo(new InternetAddress(email.getDestinatario(), true));
        helper.setSubject(conteudo.assunto());
        helper.setText(conteudo.texto(), conteudo.html());
        helper.addInline(conteudo.bannerCid(), conteudo.banner(), "image/jpeg");
        sender.send(mensagem);
    }

    private void registrarFalha(EmailBoasVindas email, Exception erro) {
        boolean definitiva = falhaDefinitiva(erro);
        int tentativas = email.getTentativas();
        boolean esgotou = tentativas >= propriedades.getMaxTentativas();
        LocalDateTime agora = LocalDateTime.now();

        if (definitiva || esgotou) {
            email.setStatus(Status.FALHOU);
        } else {
            email.setStatus(Status.PENDENTE);
            email.setProximaTentativaEm(agora.plusSeconds(espera(tentativas)));
        }
        email.setUltimoErro(resumir(erro));
        email.setAtualizadoEm(agora);
        repository.save(email);

        // Só o tipo do erro vai para o log; o detalhe fica em email_boas_vindas.ultimo_erro.
        log.warn("Falha {} ao enviar boas-vindas (usuário {}, tentativa {}/{}): {}. {}",
                definitiva ? "definitiva" : "temporária", email.getUsuarioId(), tentativas,
                propriedades.getMaxTentativas(), erro.getClass().getSimpleName(),
                email.getStatus() == Status.PENDENTE ? "Nova tentativa em " + email.getProximaTentativaEm() + "." : "Sem novas tentativas.");
    }

    /** Espera antes da próxima tentativa: base, 2x, 4x... limitada a 1 dia. */
    private long espera(int tentativasFeitas) {
        long base = Math.max(0, propriedades.getEsperaInicialSegundos());
        int expoente = Math.min(Math.max(tentativasFeitas - 1, 0), 10);
        return Math.min(base << expoente, 86_400);
    }

    /** Erros que não melhoram com nova tentativa: endereço inválido ou mensagem impossível de montar. */
    public static boolean falhaDefinitiva(Throwable erro) {
        if (erro instanceof MailSendException envio) {
            for (Exception causa : envio.getFailedMessages().values()) {
                if (falhaDefinitiva(causa)) return true;
            }
        }
        for (Throwable t = erro; t != null; t = t.getCause()) {
            if (t instanceof MailParseException || t instanceof MailPreparationException
                    || t instanceof AddressException) {
                return true;
            }
            if (t instanceof SendFailedException falha
                    && falha.getInvalidAddresses() != null && falha.getInvalidAddresses().length > 0) {
                return true;
            }
            if (t.getCause() == t) break;
        }
        return false;
    }

    private static String resumir(Throwable erro) {
        String texto = erro.getClass().getSimpleName() + (erro.getMessage() == null ? "" : ": " + erro.getMessage());
        return texto.length() > 500 ? texto.substring(0, 500) : texto;
    }

    /** "joana@exemplo.com" → "j***@exemplo.com", para não expor o endereço completo no log. */
    public static String mascarar(String email) {
        if (email == null) return "";
        int arroba = email.indexOf('@');
        if (arroba <= 0) return "***";
        return email.charAt(0) + "***" + email.substring(arroba);
    }
}
