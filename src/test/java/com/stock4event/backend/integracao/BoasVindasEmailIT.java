package com.stock4event.backend.integracao;

import com.stock4event.backend.dto.CadastroContaRequestDTO;
import com.stock4event.backend.dto.EmpresaRequestDTO;
import com.stock4event.backend.dto.UsuarioRequestDTO;
import com.stock4event.backend.entity.EmailBoasVindas;
import com.stock4event.backend.entity.EmailBoasVindas.Status;
import com.stock4event.backend.entity.Usuario;
import com.stock4event.backend.repository.EmailBoasVindasRepository;
import com.stock4event.backend.service.BoasVindasEmailService;
import com.stock4event.backend.service.CadastroService;
import com.stock4event.backend.support.IntegracaoBase;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * E-mail de boas-vindas no fluxo real de cadastro (POST /api/cadastro), com o servidor de
 * e-mail substituído por um mock: nenhum e-mail sai durante os testes.
 */
@TestPropertySource(properties = {
        "stock4event.email.habilitado=true",
        "stock4event.email.remetente=Stock4event <nao-responda@stock4event.test>",
        "stock4event.email.url-app=" + BoasVindasEmailIT.URL_APP,
        "stock4event.email.max-tentativas=3",
        "stock4event.email.espera-inicial-segundos=0",
        "stock4event.email.reprocessamento-automatico=false"
})
class BoasVindasEmailIT extends IntegracaoBase {

    static final String URL_APP = "https://app.stock4event.test/entrar";
    private static final long ESPERA_MS = 5_000;

    @MockitoBean private JavaMailSender mailSender;
    @Autowired private EmailBoasVindasRepository emailRepository;
    @Autowired private BoasVindasEmailService servico;
    @Autowired private CadastroService cadastroService;
    @Autowired private PlatformTransactionManager transacoes;

    @BeforeEach
    void prepararServidorSimulado() {
        when(mailSender.createMimeMessage())
                .thenAnswer(i -> new MimeMessage(Session.getInstance(new Properties())));
    }

    // ── Versão e destinatário ─────────────────────────────────────────────

    @Test
    void cadastroDeClienteEnviaVersaoClienteParaOProprioEmail() throws Exception {
        int n = seq();
        String email = "cliente" + n + "@teste.local";
        assertThat(status(comCorpo(POST("/api/cadastro"), cadastro("Cliente", "Joana " + n, email, n)))).isEqualTo(201);

        MimeMessage msg = mensagemEnviada();
        assertThat(destinatarios(msg)).containsExactly(email);
        assertThat(((InternetAddress) msg.getFrom()[0]).getAddress()).isEqualTo("nao-responda@stock4event.test");
        assertThat(msg.getSubject()).isEqualTo("Bem-vindo ao Stock4event! Seu evento começa aqui.");

        Partes partes = partes(msg);
        assertThat(partes.html).contains("Olá, Joana " + n + "!", "src=\"cid:banner-cliente\"",
                "href=\"" + URL_APP + "\"", "Equipe Stock4event");
        assertThat(partes.texto).contains("Seu evento começa com tudo em ordem.", "Acessar minha conta: " + URL_APP);
        assertThat(partes.imagens).containsKey("<banner-cliente>");
        assertThat(partes.imagens.get("<banner-cliente>")).startsWith("image/jpeg");

        EmailBoasVindas registro = aguardarStatus(usuarioId(email), Status.ENVIADO);
        assertThat(registro.getTipo()).isEqualTo(EmailBoasVindas.Tipo.CLIENTE);
        assertThat(registro.getTentativas()).isEqualTo(1);
        assertThat(registro.getEnviadoEm()).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Fornecedor", "ClienteFornecedor"})
    void cadastroDeFornecedorEnviaVersaoFornecedor(String perfil) throws Exception {
        int n = seq();
        String email = "fornecedor" + n + "@teste.local";
        assertThat(status(comCorpo(POST("/api/cadastro"), cadastro(perfil, "Carlos " + n, email, n)))).isEqualTo(201);

        MimeMessage msg = mensagemEnviada();
        assertThat(destinatarios(msg)).containsExactly(email);
        assertThat(msg.getSubject()).isEqualTo("Bem-vindo ao Stock4event! Vamos conectar seu negócio a novos eventos.");
        Partes partes = partes(msg);
        assertThat(partes.html).contains("Olá, Carlos " + n + "!", "src=\"cid:banner-fornecedor\"", "Boas vendas,");
        assertThat(partes.imagens).containsKey("<banner-fornecedor>");
        assertThat(aguardarStatus(usuarioId(email), Status.ENVIADO).getTipo()).isEqualTo(EmailBoasVindas.Tipo.FORNECEDOR);
    }

    @Test
    void nomeComHtmlChegaEscapado() throws Exception {
        int n = seq();
        String email = "html" + n + "@teste.local";
        assertThat(status(comCorpo(POST("/api/cadastro"),
                cadastro("Cliente", "<b>Zé</b> & <script>x()</script>", email, n)))).isEqualTo(201);

        Partes partes = partes(mensagemEnviada());
        assertThat(partes.html).contains("Olá, &lt;b&gt;Zé&lt;/b&gt; &amp; &lt;script&gt;x()&lt;/script&gt;!");
        assertThat(partes.html).doesNotContain("<script>", "<b>Zé");
    }

    // ── Casos em que nada deve ser enviado ───────────────────────────────

    @Test
    void cadastroInvalidoNaoEnvia() throws Exception {
        int n = seq();
        assertThat(status(comCorpo(POST("/api/cadastro"), cadastro("Cliente", "Ana", "ana" + n + "@semdominio", n)))).isEqualTo(400);
        assertThat(status(comCorpo(POST("/api/cadastro"), cadastro("Cliente", "Ana", "sem-arroba", n)))).isEqualTo(400);
        assertThat(status(comCorpo(POST("/api/cadastro"), cadastro("Administrador", "Ana", "ana" + n + "@teste.local", n)))).isEqualTo(400);
        Map<String, Object> semSenha = cadastro("Cliente", "Ana", "ana" + n + "@teste.local", n);
        ((Map<String, Object>) semSenha.get("usuario")).remove("senha");
        assertThat(status(comCorpo(POST("/api/cadastro"), semSenha))).isEqualTo(400);

        verify(mailSender, after(800).never()).send(any(MimeMessage.class));
        assertThat(usuarioRepository.existsByEmailIgnoreCase("ana" + n + "@teste.local")).isFalse();
    }

    @Test
    void cadastroDuplicadoNaoGeraNovoEmail() throws Exception {
        int n = seq();
        String email = "duplicado" + n + "@teste.local";
        assertThat(status(comCorpo(POST("/api/cadastro"), cadastro("Cliente", "Primeiro", email, n)))).isEqualTo(201);
        mensagemEnviada();

        int outro = seq();
        assertThat(status(comCorpo(POST("/api/cadastro"), cadastro("Fornecedor", "Mesmo e-mail", email, outro)))).isEqualTo(409);
        assertThat(status(comCorpo(POST("/api/cadastro"),
                cadastro("Fornecedor", "Mesmo CNPJ", "outro" + outro + "@teste.local", n)))).isEqualTo(409);

        verify(mailSender, after(800).times(1)).send(any(MimeMessage.class));
    }

    @Test
    void cadastroDesfeitoNaoEnvia() {
        int n = seq();
        String email = "desfeito" + n + "@teste.local";
        new TransactionTemplate(transacoes).executeWithoutResult(tx -> {
            cadastroService.cadastrarConta(dto("Cliente", "Desfeito", email, n));
            tx.setRollbackOnly(); // simula uma falha depois de salvar, que desfaz o cadastro
        });

        verify(mailSender, after(800).never()).send(any(MimeMessage.class));
        assertThat(usuarioRepository.existsByEmailIgnoreCase(email)).isFalse();
        assertThat(emailRepository.findAll()).noneMatch(e -> e.getDestinatario().equals(email));
    }

    // ── Falhas e novas tentativas ─────────────────────────────────────────

    @Test
    void falhaNoEnvioNaoDesfazAContaCriada() throws Exception {
        doThrow(new MailSendException("SMTP indisponível")).when(mailSender).send(any(MimeMessage.class));
        int n = seq();
        String email = "semsmtp" + n + "@teste.local";

        assertThat(status(comCorpo(POST("/api/cadastro"), cadastro("Cliente", "Sem SMTP", email, n)))).isEqualTo(201);
        EmailBoasVindas registro = aguardarTentativas(usuarioId(email), 1);

        assertThat(registro.getStatus()).isEqualTo(Status.PENDENTE); // falha temporária: fica para nova tentativa
        assertThat(registro.getUltimoErro()).contains("SMTP indisponível");
        assertThat(usuarioRepository.existsByEmailIgnoreCase(email)).isTrue();
        assertThat(status(comCorpo(POST("/api/usuarios/login"), Map.of("email", email, "senha", SENHA)))).isEqualTo(200);
    }

    @Test
    void novasTentativasNaoDuplicamOEmail() throws Exception {
        // 1ª tentativa falha; as seguintes funcionam.
        doThrow(new MailSendException("timeout")).doNothing().when(mailSender).send(any(MimeMessage.class));
        int n = seq();
        String email = "retentativa" + n + "@teste.local";
        assertThat(status(comCorpo(POST("/api/cadastro"), cadastro("Fornecedor", "Retentativa", email, n)))).isEqualTo(201);
        Long id = aguardarTentativas(usuarioId(email), 1).getId();

        // Várias execuções ao mesmo tempo (agendador + chamadas diretas): só uma envia.
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Boolean>> tarefas = new ArrayList<>();
            for (int i = 0; i < 4; i++) tarefas.add(() -> servico.processar(id));
            int enviados = 0;
            for (Future<Boolean> f : pool.invokeAll(tarefas)) if (f.get()) enviados++;
            assertThat(enviados).isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }
        servico.reprocessarPendentes();
        assertThat(servico.processar(id)).isFalse();

        verify(mailSender, times(2)).send(any(MimeMessage.class)); // a que falhou + a que foi aceita
        EmailBoasVindas registro = emailRepository.findById(id).orElseThrow();
        assertThat(registro.getStatus()).isEqualTo(Status.ENVIADO);
        assertThat(registro.getTentativas()).isEqualTo(2);
    }

    @Test
    void falhasTemporariasParamNoLimiteDeTentativas() throws Exception {
        doThrow(new MailSendException("timeout")).when(mailSender).send(any(MimeMessage.class));
        int n = seq();
        String email = "limite" + n + "@teste.local";
        assertThat(status(comCorpo(POST("/api/cadastro"), cadastro("Cliente", "Limite", email, n)))).isEqualTo(201);
        Long id = aguardarTentativas(usuarioId(email), 1).getId();

        assertThat(servico.processar(id)).isFalse(); // 2ª
        assertThat(servico.processar(id)).isFalse(); // 3ª (limite configurado no teste)
        assertThat(servico.processar(id)).isFalse(); // não tenta mais

        verify(mailSender, times(3)).send(any(MimeMessage.class));
        EmailBoasVindas registro = emailRepository.findById(id).orElseThrow();
        assertThat(registro.getStatus()).isEqualTo(Status.FALHOU);
        assertThat(registro.getTentativas()).isEqualTo(3);
    }

    @Test
    void falhaDefinitivaNaoTentaDeNovo() throws Exception {
        doThrow(new MailParseException("endereço recusado")).when(mailSender).send(any(MimeMessage.class));
        int n = seq();
        String email = "definitiva" + n + "@teste.local";
        assertThat(status(comCorpo(POST("/api/cadastro"), cadastro("Cliente", "Definitiva", email, n)))).isEqualTo(201);

        EmailBoasVindas registro = aguardarStatus(usuarioId(email), Status.FALHOU);
        assertThat(servico.processar(registro.getId())).isFalse();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ── Apoio ─────────────────────────────────────────────────────────────

    private Map<String, Object> cadastro(String perfil, String nome, String email, int n) {
        Map<String, Object> usuario = new HashMap<>(Map.of("nome", nome, "email", email, "senha", SENHA));
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("perfil", perfil);
        corpo.put("empresa", Map.of("nome", "Empresa Boas-vindas " + n, "cnpj", cnpj(n)));
        corpo.put("usuario", usuario);
        return corpo;
    }

    private CadastroContaRequestDTO dto(String perfil, String nome, String email, int n) {
        EmpresaRequestDTO empresa = new EmpresaRequestDTO();
        empresa.setNome("Empresa Boas-vindas " + n);
        empresa.setCnpj(cnpj(n));
        UsuarioRequestDTO usuario = new UsuarioRequestDTO();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(SENHA);
        CadastroContaRequestDTO dto = new CadastroContaRequestDTO();
        dto.setPerfil(perfil);
        dto.setEmpresa(empresa);
        dto.setUsuario(usuario);
        return dto;
    }

    private static String cnpj(int n) {
        return String.format("66.%03d.%03d/0001-00", n / 1000, n % 1000);
    }

    private MimeMessage mensagemEnviada() {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, timeout(ESPERA_MS)).send(captor.capture());
        return captor.getValue();
    }

    private Long usuarioId(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email).map(Usuario::getId).orElseThrow();
    }

    /** O envio é assíncrono: espera o registro chegar ao status esperado. */
    private EmailBoasVindas aguardarStatus(Long usuarioId, Status esperado) throws InterruptedException {
        return aguardar(usuarioId, e -> e.getStatus() == esperado);
    }

    private EmailBoasVindas aguardarTentativas(Long usuarioId, int tentativas) throws InterruptedException {
        return aguardar(usuarioId, e -> e.getTentativas() >= tentativas && e.getStatus() != Status.ENVIANDO);
    }

    private EmailBoasVindas aguardar(Long usuarioId, java.util.function.Predicate<EmailBoasVindas> condicao)
            throws InterruptedException {
        long limite = System.currentTimeMillis() + ESPERA_MS;
        EmailBoasVindas atual = null;
        while (System.currentTimeMillis() < limite) {
            atual = emailRepository.findByUsuarioId(usuarioId).orElse(null);
            if (atual != null && condicao.test(atual)) return atual;
            Thread.sleep(50);
        }
        throw new AssertionError("Registro de boas-vindas não chegou ao estado esperado: " + atual);
    }

    private static List<String> destinatarios(MimeMessage msg) throws Exception {
        List<String> lista = new ArrayList<>();
        for (var a : msg.getAllRecipients()) lista.add(((InternetAddress) a).getAddress());
        return lista;
    }

    private static final class Partes {
        String html = "";
        String texto = "";
        final Map<String, String> imagens = new HashMap<>(); // Content-ID → Content-Type
    }

    private static Partes partes(MimeMessage msg) throws Exception {
        msg.saveChanges(); // gera os cabeçalhos Content-Type das partes, como no envio real
        Partes p = new Partes();
        coletar(msg, p);
        return p;
    }

    private static void coletar(Part parte, Partes p) throws Exception {
        if (parte.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) parte.getContent();
            for (int i = 0; i < mp.getCount(); i++) coletar(mp.getBodyPart(i), p);
        } else if (parte.isMimeType("text/html")) {
            p.html = (String) parte.getContent();
        } else if (parte.isMimeType("text/plain")) {
            p.texto = (String) parte.getContent();
        } else if (parte instanceof BodyPart corpo && corpo.getHeader("Content-ID") != null) {
            p.imagens.put(corpo.getHeader("Content-ID")[0], corpo.getContentType());
        }
    }
}
