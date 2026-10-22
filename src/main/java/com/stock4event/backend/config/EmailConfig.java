package com.stock4event.backend.config;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Liga o envio assíncrono e o agendador de novas tentativas do e-mail de boas-vindas e,
 * com o envio habilitado, confere a configuração ao subir: melhor falhar na inicialização,
 * com a variável que falta, do que descobrir no primeiro cadastro. Nenhum segredo é registrado no log.
 */
@Configuration
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(EmailProperties.class)
public class EmailConfig implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(EmailConfig.class);

    private final EmailProperties propriedades;
    private final ObjectProvider<JavaMailSender> mailSender;

    public EmailConfig(EmailProperties propriedades, ObjectProvider<JavaMailSender> mailSender) {
        this.propriedades = propriedades;
        this.mailSender = mailSender;
    }

    @Override
    public void afterPropertiesSet() {
        if (!propriedades.isHabilitado()) {
            log.info("E-mail de boas-vindas desabilitado (STOCK4EVENT_EMAIL_HABILITADO=false).");
            return;
        }
        List<String> problemas = validar(propriedades, mailSender.getIfAvailable() != null);
        if (!problemas.isEmpty()) {
            throw new IllegalStateException("Configuração do e-mail de boas-vindas incompleta: "
                    + String.join(" ", problemas));
        }
        log.info("E-mail de boas-vindas habilitado; botão aponta para {}", propriedades.getUrlApp());
    }

    /** Lista o que falta para enviar; vazia quando a configuração está completa. */
    public static List<String> validar(EmailProperties p, boolean servidorSmtpConfigurado) {
        List<String> problemas = new ArrayList<>();
        if (!servidorSmtpConfigurado) {
            problemas.add("Informe o servidor SMTP em SPRING_MAIL_HOST (e SPRING_MAIL_PORT, SPRING_MAIL_USERNAME, SPRING_MAIL_PASSWORD).");
        }
        if (!remetenteValido(p.getRemetente())) {
            problemas.add("Informe um remetente válido em STOCK4EVENT_EMAIL_REMETENTE, ex.: Stock4event <nao-responda@seudominio.com.br>.");
        }
        if (!urlValida(p.getUrlApp())) {
            problemas.add("Informe a URL de acesso ao aplicativo (http/https) em STOCK4EVENT_APP_URL.");
        }
        if (p.getMaxTentativas() < 1) {
            problemas.add("stock4event.email.max-tentativas deve ser pelo menos 1.");
        }
        return problemas;
    }

    static boolean remetenteValido(String remetente) {
        if (remetente == null || remetente.isBlank()) return false;
        try {
            InternetAddress endereco = new InternetAddress(remetente.trim(), true);
            return endereco.getAddress() != null && endereco.getAddress().contains("@");
        } catch (AddressException e) {
            return false;
        }
    }

    static boolean urlValida(String url) {
        if (url == null || url.isBlank()) return false;
        try {
            URI uri = URI.create(url.trim());
            String esquema = uri.getScheme();
            return ("https".equalsIgnoreCase(esquema) || "http".equalsIgnoreCase(esquema))
                    && uri.getHost() != null && !uri.getHost().isBlank();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
