package com.stock4event.backend.integracao;

import com.fasterxml.jackson.databind.JsonNode;
import com.stock4event.backend.entity.Barraca;
import com.stock4event.backend.entity.EstoqueBarraca;
import com.stock4event.backend.entity.Produto;
import com.stock4event.backend.support.IntegracaoBase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Vitrine entre contas: quiosques de outras empresas e produtos recém-cadastrados pelos fornecedores. */
class VitrineMarketplaceIT extends IntegracaoBase {

    private void alocar(Barraca b, Produto p, String qtd) {
        EstoqueBarraca e = new EstoqueBarraca();
        e.setBarraca(b); e.setProduto(p); e.setQuantidade(new BigDecimal(qtd));
        estoqueBarracaRepository.save(e);
    }

    private JsonNode quiosquePorId(JsonNode lista, long id) {
        for (JsonNode q : lista) if (q.path("id").asLong() == id) return q;
        return null;
    }

    @Test
    void quiosquesDeOutrasContasAparecemSemOsDaPropriaEmpresaNemDesativados() throws Exception {
        Conta eu = novoCliente();
        Conta vizinho = novoCliente();
        Conta distribuidora = novoFornecedor();
        Barraca meu = novaBarraca(eu);
        Barraca doVizinho = novaBarraca(vizinho);
        Barraca daDistribuidora = novaBarraca(distribuidora);
        Barraca desativado = novaBarraca(vizinho);
        desativado.setAtiva(0);
        barracaRepository.save(desativado);

        Produto cerveja = novoProduto(vizinho.empresa(), "10", "5.00");
        Produto esgotado = novoProduto(vizinho.empresa(), "4", "3.00");
        alocar(doVizinho, cerveja, "6");
        alocar(doVizinho, esgotado, "0");

        JsonNode lista = corpo(chamar(auth(GET("/api/marketplace/quiosques"), eu)));
        assertThat(quiosquePorId(lista, meu.getId())).isNull();
        assertThat(quiosquePorId(lista, desativado.getId())).isNull();
        assertThat(quiosquePorId(lista, daDistribuidora.getId()).path("distribuidora").asBoolean()).isTrue();

        JsonNode q = quiosquePorId(lista, doVizinho.getId());
        assertThat(q).isNotNull();
        assertThat(q.path("empresaNome").asText()).isEqualTo(vizinho.empresa().getNome());
        assertThat(q.path("distribuidora").asBoolean()).isFalse();
        assertThat(q.path("totalProdutos").asInt()).isEqualTo(2);
        assertThat(q.path("produtosDisponiveis").asInt()).isEqualTo(1);
        // estoque interno de outra empresa não é exposto: só "disponível" sim/não
        assertThat(q.toString()).doesNotContain("quantidade", "\"estoque\"");
        for (JsonNode item : q.path("produtos")) {
            boolean deveEstarDisponivel = item.path("produtoId").asLong() == cerveja.getId();
            assertThat(item.path("disponivel").asBoolean()).isEqualTo(deveEstarDisponivel);
        }

        // o vizinho, por sua vez, enxerga o meu quiosque
        assertThat(quiosquePorId(corpo(chamar(auth(GET("/api/marketplace/quiosques"), vizinho))), meu.getId())).isNotNull();
    }

    @Test
    void buscaDeQuiosquesPorNomeEmpresaOuProduto() throws Exception {
        Conta eu = novoCliente();
        Conta outro = novoCliente();
        Barraca b = novaBarraca(outro);
        b.setNome("Barraca do Forró " + seq());
        barracaRepository.save(b);
        Produto p = novoProduto(outro.empresa(), "5", "2.00");
        p.setNome("Caldo de Cana " + seq());
        produtoRepository.save(p);
        alocar(b, p, "5");

        assertThat(quiosquePorId(corpo(chamar(auth(GET("/api/marketplace/quiosques?busca=forro"), eu))), b.getId())).isNotNull();
        assertThat(quiosquePorId(corpo(chamar(auth(GET("/api/marketplace/quiosques?busca=caldo de cana"), eu))), b.getId())).isNotNull();
        assertThat(quiosquePorId(corpo(chamar(auth(GET("/api/marketplace/quiosques?busca=nada-com-isso"), eu))), b.getId())).isNull();
        assertThat(status(auth(GET("/api/marketplace/quiosques?busca=" + "x".repeat(101)), eu))).isEqualTo(400);
    }

    @Test
    void produtosRecentesSaoDosFornecedoresMaisNovosPrimeiroSemOsDaPropriaEmpresa() throws Exception {
        Conta fornecedor = novoFornecedor();
        Conta outroFornecedor = novoFornecedor();
        Conta cliente = novoCliente();

        // cadastrados pela API, na ordem: antigo → novo
        List<Long> ids = new ArrayList<>();
        for (String nome : List.of("Refri Antigo", "Suco Intermediário", "Água Mais Nova")) {
            long id = corpo(chamar(comCorpo(auth(POST("/api/produtos"), fornecedor), Map.of(
                    "nome", nome + " " + seq(), "precoVenda", 3, "unidade", "UN", "estoque", 10)))).path("id").asLong();
            ids.add(id);
            Thread.sleep(15);
        }
        Produto doCliente = novoProduto(cliente.empresa(), "5", "1.00");   // cliente não é fornecedor
        Produto inativo = novoProduto(outroFornecedor.empresa(), "5", "1.00");
        inativo.setAtivo(0);
        produtoRepository.save(inativo);

        JsonNode lista = corpo(chamar(auth(GET("/api/marketplace/produtos-recentes?limite=60"), outroFornecedor)));
        List<Long> recebidos = new ArrayList<>();
        lista.forEach(p -> recebidos.add(p.path("id").asLong()));
        assertThat(recebidos).doesNotContain(doCliente.getId(), inativo.getId());
        assertThat(recebidos.indexOf(ids.get(2))).isLessThan(recebidos.indexOf(ids.get(1)));
        assertThat(recebidos.indexOf(ids.get(1))).isLessThan(recebidos.indexOf(ids.get(0)));
        JsonNode primeiro = lista.get(recebidos.indexOf(ids.get(2)));
        assertThat(primeiro.path("fornecedorNome").asText()).isEqualTo(fornecedor.empresa().getNome());
        assertThat(primeiro.path("empresaId").asLong()).isEqualTo(fornecedor.empresaId());
        assertThat(primeiro.path("criadoEm").isNull()).isFalse();

        assertThat(primeiro.path("proprio").asBoolean()).isFalse();

        // o próprio fornecedor também vê os seus produtos, marcados como próprios
        JsonNode vistaDoFornecedor = corpo(chamar(auth(GET("/api/marketplace/produtos-recentes?limite=60"), fornecedor)));
        List<Long> proprios = new ArrayList<>();
        vistaDoFornecedor.forEach(p -> { if (p.path("proprio").asBoolean()) proprios.add(p.path("id").asLong()); });
        assertThat(proprios).containsAll(ids);

        assertThat(corpo(chamar(auth(GET("/api/marketplace/produtos-recentes?limite=2"), cliente)))).hasSize(2);
    }

    @Test
    void produtoCadastradoPorContaClienteEFornecedorApareceParaTodos() throws Exception {
        // cadastro real de uma conta "Cliente e fornecedor" (como a demo): a empresa vira AMBOS
        int n = seq();
        String email = "ambos" + n + "@teste.local";
        assertThat(status(comCorpo(POST("/api/cadastro"), Map.of("perfil", "ClienteFornecedor",
                "empresa", Map.of("nome", "Mercado Ambos " + n, "cnpj", String.format("66.%03d.000/0001-00", n)),
                "usuario", Map.of("nome", "Dono", "email", email, "senha", "senha123"))))).isEqualTo(201);
        String token = corpo(chamar(comCorpo(POST("/api/usuarios/login"), Map.of("email", email, "senha", "senha123")))).path("token").asText();
        var conta = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        Conta ambos = new Conta(conta.getEmpresa(), conta, token);
        assertThat(conta.getEmpresa().getTipo()).isEqualTo("AMBOS");

        // cadastra um produto na visão de fornecedor…
        long id = corpo(chamar(comCorpo(auth(POST("/api/produtos"), ambos), Map.of(
                "nome", "Produto da Visão Fornecedor " + n, "precoVenda", 7.5, "unidade", "UN", "estoque", 12)))).path("id").asLong();

        // …e ele aparece no topo para outro cliente, sem ser "próprio"
        Conta outroCliente = novoCliente();
        JsonNode paraOutro = corpo(chamar(auth(GET("/api/marketplace/produtos-recentes"), outroCliente)));
        assertThat(paraOutro.get(0).path("id").asLong()).isEqualTo(id);
        assertThat(paraOutro.get(0).path("proprio").asBoolean()).isFalse();
        assertThat(paraOutro.get(0).path("fornecedorNome").asText()).startsWith("Mercado Ambos");

        // na visão de cliente da mesma conta ele também aparece, marcado como próprio
        JsonNode paraSiMesma = corpo(chamar(auth(GET("/api/marketplace/produtos-recentes"), ambos)));
        assertThat(paraSiMesma.get(0).path("id").asLong()).isEqualTo(id);
        assertThat(paraSiMesma.get(0).path("proprio").asBoolean()).isTrue();
    }

    @Test
    void vitrineExigeLogin() throws Exception {
        assertThat(status(GET("/api/marketplace/quiosques"))).isEqualTo(401);
        assertThat(status(GET("/api/marketplace/produtos-recentes"))).isEqualTo(401);
    }
}
