package com.stock4event.backend.service;

import com.stock4event.backend.config.EmailProperties;
import com.stock4event.backend.entity.EmailBoasVindas.Tipo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

/**
 * Monta o e-mail de boas-vindas (assunto, HTML, texto simples e banner) para cada versão.
 *
 * <p>Os modelos ficam em src/main/resources/email/boas-vindas/. O banner é anexado como imagem
 * incorporada (CID), então o envio não depende de hospedagem pública nem de caminhos locais.
 * O nome do usuário é escapado no HTML e limpo de caracteres de controle no texto simples.</p>
 */
@Component
public class BoasVindasTemplate {

    static final String PASTA = "email/boas-vindas/";

    /** Conteúdo pronto para envio. */
    public record EmailRenderizado(String assunto, String html, String texto,
                                   String bannerCid, Resource banner) {}

    private record Modelo(String assunto, String arquivo, String bannerCid, String bannerArquivo) {}

    private static final Map<Tipo, Modelo> MODELOS = new EnumMap<>(Map.of(
            Tipo.CLIENTE, new Modelo(
                    "Bem-vindo ao Stock4event! Seu evento começa aqui.",
                    "cliente", "banner-cliente", "banner-cliente.jpg"),
            Tipo.FORNECEDOR, new Modelo(
                    "Bem-vindo ao Stock4event! Vamos conectar seu negócio a novos eventos.",
                    "fornecedor", "banner-fornecedor", "banner-fornecedor.jpg")
    ));

    private final EmailProperties propriedades;
    private final Map<Tipo, String> html = new EnumMap<>(Tipo.class);
    private final Map<Tipo, String> texto = new EnumMap<>(Tipo.class);

    public BoasVindasTemplate(EmailProperties propriedades) {
        this.propriedades = propriedades;
        // Lê os modelos uma vez, ao subir: um arquivo ausente falha na inicialização, não no envio.
        MODELOS.forEach((tipo, modelo) -> {
            html.put(tipo, ler(PASTA + modelo.arquivo() + ".html"));
            texto.put(tipo, ler(PASTA + modelo.arquivo() + ".txt"));
            if (!new ClassPathResource(PASTA + modelo.bannerArquivo()).exists()) {
                throw new IllegalStateException("Banner do e-mail não encontrado: " + modelo.bannerArquivo());
            }
        });
    }

    public EmailRenderizado renderizar(Tipo tipo, String nome) {
        Modelo modelo = MODELOS.get(tipo);
        String nomeLimpo = limparNome(nome);
        String url = propriedades.getUrlApp() == null ? "" : propriedades.getUrlApp().trim();

        String saudacaoHtml = nomeLimpo.isEmpty() ? "Olá!" : "Olá, " + HtmlUtils.htmlEscape(nomeLimpo, "UTF-8") + "!";
        String saudacaoTexto = nomeLimpo.isEmpty() ? "Olá!" : "Olá, " + nomeLimpo + "!";

        String corpoHtml = html.get(tipo)
                .replace("{{SAUDACAO}}", saudacaoHtml)
                .replace("{{URL_APP}}", HtmlUtils.htmlEscape(url, "UTF-8"))
                .replace("{{BANNER_CID}}", modelo.bannerCid());
        String corpoTexto = texto.get(tipo)
                .replace("{{SAUDACAO}}", saudacaoTexto)
                .replace("{{URL_APP}}", url);

        return new EmailRenderizado(modelo.assunto(), corpoHtml, corpoTexto, modelo.bannerCid(),
                new ClassPathResource(PASTA + modelo.bannerArquivo()));
    }

    /** Remove quebras de linha e outros caracteres de controle e junta espaços repetidos. */
    static String limparNome(String nome) {
        if (nome == null) return "";
        return nome.replaceAll("\\p{Cntrl}", " ").replaceAll("\\s+", " ").trim();
    }

    private static String ler(String caminho) {
        try {
            return new String(new ClassPathResource(caminho).getContentAsByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Modelo de e-mail não encontrado: " + caminho, e);
        }
    }
}
