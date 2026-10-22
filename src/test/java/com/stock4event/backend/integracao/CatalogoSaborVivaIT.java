package com.stock4event.backend.integracao;

import com.fasterxml.jackson.databind.JsonNode;
import com.stock4event.backend.entity.Empresa;
import com.stock4event.backend.support.IntegracaoBase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** A SaborViva entra pelo data.sql e pelos runners, com o catálogo do PDF (15 produtos, 4.360 un., R$ 37.303,00). */
class CatalogoSaborVivaIT extends IntegracaoBase {

    private static final String CNPJ = "30.004.004/0001-04";

    @Test
    void saborVivaApareceNoMarketplaceComOCatalogoCompleto() throws Exception {
        Empresa sv = empresaRepository.findByCnpj(CNPJ).orElseThrow();
        assertThat(sv.getNome()).isEqualTo("SaborViva");
        assertThat(sv.getTipo()).isEqualTo("DISTRIBUIDOR");
        assertThat(sv.getLogoUrl()).isEqualTo("/uploads/empresas/saborviva-logo.png");

        Conta cliente = novoCliente();
        JsonNode fornecedores = corpo(chamar(auth(GET("/api/marketplace/fornecedores"), cliente)));
        JsonNode encontrado = null;
        for (JsonNode f : fornecedores) if (f.path("id").asLong() == sv.getId()) encontrado = f;
        assertThat(encontrado).isNotNull();
        assertThat(encontrado.path("totalProdutos").asInt()).isEqualTo(15);

        JsonNode produtos = corpo(chamar(auth(GET("/api/marketplace/fornecedores/" + sv.getId() + "/produtos"), cliente)));
        assertThat(produtos).hasSize(15);
        BigDecimal unidades = BigDecimal.ZERO;
        BigDecimal valor = BigDecimal.ZERO;
        for (JsonNode p : produtos) {
            assertThat(p.path("codigo").asText()).startsWith("MKT-SV-");
            assertThat(p.path("imagemUrl").asText()).startsWith("/uploads/produtos/saborviva-");
            unidades = unidades.add(p.path("estoque").decimalValue());
            valor = valor.add(p.path("precoVenda").decimalValue().multiply(p.path("estoque").decimalValue()));
        }
        assertThat(unidades).isEqualByComparingTo("4360");
        assertThat(valor).isEqualByComparingTo("37303.00");
        assertThat(produtos.toString()).contains("Água mineral sem gás 500 ml", "Suco de uva integral 1 L", "Fanta Laranja 2 L");
    }

    @Test
    void casaDosVinhosTemONovoCatalogoSemAlcoolEOsVinhosAntigosSaemDaVitrine() throws Exception {
        Empresa cdv = empresaRepository.findByCnpj("30.001.001/0001-01").orElseThrow();
        Conta cliente = novoCliente();
        JsonNode produtos = corpo(chamar(auth(GET("/api/marketplace/fornecedores/" + cdv.getId() + "/produtos"), cliente)));
        assertThat(produtos).hasSize(15);
        BigDecimal unidades = BigDecimal.ZERO;
        BigDecimal valor = BigDecimal.ZERO;
        for (JsonNode p : produtos) {
            assertThat(p.path("codigo").asText()).matches("MKT-CDV-0(0[6-9]|1\\d|20)");
            unidades = unidades.add(p.path("estoque").decimalValue());
            valor = valor.add(p.path("precoVenda").decimalValue().multiply(p.path("estoque").decimalValue()));
        }
        assertThat(unidades).isEqualByComparingTo("915");
        assertThat(valor).isEqualByComparingTo("40983.50");
        assertThat(produtos.toString()).contains("Suco de uva integral Serra Gaúcha 1 L", "Vinho tinto desalcoolizado premium 750 ml");
        assertThat(produtos.toString()).doesNotContain("Mazzei", "Louis Latour", "Carmin");
        assertThat(cdv.getDescricao()).contains("sem álcool");
    }

    @Test
    void imagensDaSaborVivaSaoServidas() throws Exception {
        assertThat(status(GET("/uploads/empresas/saborviva-logo.png"))).isEqualTo(200);
        assertThat(status(GET("/uploads/empresas/saborviva-capa.png"))).isEqualTo(200);
        assertThat(status(GET("/uploads/produtos/saborviva-coca-cola-original-2l.png"))).isEqualTo(200);
    }
}
