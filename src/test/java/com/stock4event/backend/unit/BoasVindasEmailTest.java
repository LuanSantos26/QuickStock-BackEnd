package com.stock4event.backend.unit;

import com.stock4event.backend.config.EmailConfig;
import com.stock4event.backend.config.EmailProperties;
import com.stock4event.backend.entity.EmailBoasVindas.Tipo;
import com.stock4event.backend.service.BoasVindasEmailService;
import com.stock4event.backend.service.BoasVindasTemplate;
import com.stock4event.backend.service.BoasVindasTemplate.EmailRenderizado;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;

import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Modelos, configuração e regras auxiliares do e-mail de boas-vindas, sem subir o Spring. */
class BoasVindasEmailTest {

    private static final String URL = "https://app.stock4event.test/entrar";

    private static BoasVindasTemplate template(String url) {
        EmailProperties p = new EmailProperties();
        p.setUrlApp(url);
        return new BoasVindasTemplate(p);
    }

    @Nested
    class Modelos {

        @Test
        void clienteUsaAssuntoTextosEBannerAmarelo() {
            EmailRenderizado e = template(URL).renderizar(Tipo.CLIENTE, "Joana");
            assertThat(e.assunto()).isEqualTo("Bem-vindo ao Stock4event! Seu evento começa aqui.");
            assertThat(e.bannerCid()).isEqualTo("banner-cliente");
            assertThat(e.banner().getFilename()).isEqualTo("banner-cliente.jpg");
            assertThat(e.html()).contains("Olá, Joana!", "organize seus quiosques", "Até já,", "Equipe Stock4event");
            assertThat(e.texto()).contains("Seu evento começa com tudo em ordem.", "Olá, Joana!", "Equipe Stock4event");
        }

        @Test
        void fornecedorUsaAssuntoTextosEBannerAzul() {
            EmailRenderizado e = template(URL).renderizar(Tipo.FORNECEDOR, "Carlos");
            assertThat(e.assunto()).isEqualTo("Bem-vindo ao Stock4event! Vamos conectar seu negócio a novos eventos.");
            assertThat(e.bannerCid()).isEqualTo("banner-fornecedor");
            assertThat(e.banner().getFilename()).isEqualTo("banner-fornecedor.jpg");
            assertThat(e.html()).contains("Olá, Carlos!", "cadastre seus primeiros produtos", "Boas vendas,", "Equipe Stock4event");
            assertThat(e.texto()).contains("Novas conexões. Mais oportunidades.", "Olá, Carlos!");
        }

        @ParameterizedTest
        @EnumSource(Tipo.class)
        void botaoEImagensUsamUrlConfiguradaECid(Tipo tipo) throws Exception {
            EmailRenderizado e = template(URL).renderizar(tipo, "Ana");
            String cid = e.bannerCid();

            assertThat(e.html()).contains("src=\"cid:" + cid + "\"");
            assertThat(e.html()).contains("href=\"" + URL + "\"");
            assertThat(e.html()).containsIgnoringCase("acessar minha conta");
            assertThat(e.texto()).contains("Acessar minha conta: " + URL);
            // Sem caminhos locais nem marcadores esquecidos.
            assertThat(e.html()).doesNotContain("{{", "file:", "C:\\", "localhost");
            assertThat(e.texto()).doesNotContain("{{");
            // Imagem com texto alternativo e o banner leve o bastante para e-mail.
            assertThat(e.html()).containsPattern("<img [^>]*alt=\"Stock4event para [^\"]+\"");
            try (InputStream in = e.banner().getInputStream()) {
                byte[] bytes = in.readAllBytes();
                assertThat(bytes.length).isBetween(10_000, 300_000);
                assertThat(bytes[0] & 0xFF).isEqualTo(0xFF); // assinatura JPEG
                assertThat(bytes[1] & 0xFF).isEqualTo(0xD8);
            }
        }

        @Test
        void nomeEEscapadoNoHtml() {
            EmailRenderizado e = template(URL).renderizar(Tipo.CLIENTE, "<script>alert('x')</script> & \"Zé\"");
            assertThat(e.html()).contains("Olá, &lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt; &amp; &quot;Zé&quot;!");
            assertThat(e.html()).doesNotContain("<script>");
        }

        @Test
        void nomeSemQuebrasDeLinhaNoTextoSimples() {
            EmailRenderizado e = template(URL).renderizar(Tipo.FORNECEDOR, "Ana\r\nBcc: invasor@x.com\t ");
            assertThat(e.texto()).contains("Olá, Ana Bcc: invasor@x.com!");
            assertThat(e.texto()).doesNotContain("Ana\r\n");
        }

        @Test
        void nomeVazioViraSaudacaoGenerica() {
            assertThat(template(URL).renderizar(Tipo.CLIENTE, "   ").html()).contains(">Olá!<");
        }

        @Test
        void urlComCaracteresEspeciaisEEscapadaNoAtributo() {
            EmailRenderizado e = template("https://app.test/entrar?a=1&b=\"x\"").renderizar(Tipo.CLIENTE, "Ana");
            assertThat(e.html()).contains("href=\"https://app.test/entrar?a=1&amp;b=&quot;x&quot;\"");
        }
    }

    @Nested
    class Configuracao {

        private EmailProperties completa() {
            EmailProperties p = new EmailProperties();
            p.setHabilitado(true);
            p.setRemetente("Stock4event <nao-responda@stock4event.test>");
            p.setUrlApp(URL);
            return p;
        }

        @Test
        void configuracaoCompletaNaoTemPendencias() {
            assertThat(EmailConfig.validar(completa(), true)).isEmpty();
        }

        @Test
        void apontaCadaVariavelQueFalta() {
            EmailProperties p = new EmailProperties();
            p.setHabilitado(true);
            assertThat(String.join(" ", EmailConfig.validar(p, false)))
                    .contains("SPRING_MAIL_HOST", "STOCK4EVENT_EMAIL_REMETENTE", "STOCK4EVENT_APP_URL");
        }

        @Test
        void recusaUrlSemHttpERemetenteInvalido() {
            EmailProperties p = completa();
            p.setUrlApp("ftp://app.stock4event.test");
            p.setRemetente("sem-arroba");
            assertThat(String.join(" ", EmailConfig.validar(p, true)))
                    .contains("STOCK4EVENT_APP_URL", "STOCK4EVENT_EMAIL_REMETENTE");
            p.setUrlApp("C:\\Users\\app.html");
            assertThat(String.join(" ", EmailConfig.validar(p, true))).contains("STOCK4EVENT_APP_URL");
        }
    }

    @Nested
    class Regras {

        @Test
        void versaoPeloPerfilSalvo() {
            assertThat(BoasVindasEmailService.tipoPorPerfil("Cliente")).contains(Tipo.CLIENTE);
            assertThat(BoasVindasEmailService.tipoPorPerfil("Fornecedor")).contains(Tipo.FORNECEDOR);
            assertThat(BoasVindasEmailService.tipoPorPerfil("ClienteFornecedor")).contains(Tipo.FORNECEDOR);
            assertThat(BoasVindasEmailService.tipoPorPerfil("Admin")).isEqualTo(Optional.empty());
            assertThat(BoasVindasEmailService.tipoPorPerfil(null)).isEmpty();
        }

        @Test
        void classificaFalhasTemporariasEDefinitivas() {
            assertThat(BoasVindasEmailService.falhaDefinitiva(new MailSendException("Connection timed out"))).isFalse();
            assertThat(BoasVindasEmailService.falhaDefinitiva(new MailAuthenticationException("535"))).isFalse();
            assertThat(BoasVindasEmailService.falhaDefinitiva(new MailParseException("endereço inválido"))).isTrue();
        }

        @Test
        void mascaraEnderecoNoLog() {
            assertThat(BoasVindasEmailService.mascarar("joana@exemplo.com")).isEqualTo("j***@exemplo.com");
            assertThat(BoasVindasEmailService.mascarar("sem-arroba")).isEqualTo("***");
        }
    }
}
