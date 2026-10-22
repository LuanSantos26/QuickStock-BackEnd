package com.stock4event.backend.integracao;

import com.fasterxml.jackson.databind.JsonNode;
import com.stock4event.backend.entity.*;
import com.stock4event.backend.support.IntegracaoBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/** PDV: busca, totais e troco, estoque, isolamento, idempotência, concorrência e transação. */
class PdvIT extends IntegracaoBase {

    private Conta loja;
    private Barraca quiosque;
    private Produto cerveja;   // R$ 4,50 · 10 no quiosque · 30 no total
    private Produto agua;      // R$ 2,25 · 5 no quiosque · 5 no total

    @BeforeEach
    void dados() {
        loja = novoCliente();
        quiosque = novaBarraca(loja);
        cerveja = novoProduto(loja.empresa(), "30", "4.50");
        cerveja.setNome("Cerveja Pilsen 350ml");
        cerveja = produtoRepository.save(cerveja);
        agua = novoProduto(loja.empresa(), "5", "2.25");
        agua.setNome("Água Mineral 500ml");
        agua = produtoRepository.save(agua);
        alocar(quiosque, cerveja, "10");
        alocar(quiosque, agua, "5");
    }

    private EstoqueBarraca alocar(Barraca b, Produto p, String qtd) {
        EstoqueBarraca e = new EstoqueBarraca();
        e.setBarraca(b); e.setProduto(p); e.setQuantidade(new BigDecimal(qtd));
        return estoqueBarracaRepository.save(e);
    }

    private BigDecimal noQuiosque(Produto p) {
        return estoqueBarracaRepository.findByBarracaIdAndProdutoId(quiosque.getId(), p.getId()).orElseThrow().getQuantidade();
    }

    private BigDecimal noProduto(Produto p) {
        return produtoRepository.findById(p.getId()).orElseThrow().getEstoque();
    }

    private Map<String, Object> venda(String forma, Object... itens) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("barracaId", quiosque.getId());
        v.put("chaveVenda", UUID.randomUUID().toString());
        v.put("formaPagamento", forma);
        List<Map<String, Object>> lista = new ArrayList<>();
        for (int i = 0; i < itens.length; i += 2) {
            lista.add(new LinkedHashMap<>(Map.of("produtoId", itens[i], "quantidade", itens[i + 1])));
        }
        v.put("itens", lista);
        return v;
    }

    private MvcResult vender(Conta c, Map<String, Object> corpo) throws Exception {
        return chamar(comCorpo(auth(POST("/api/pdv/vendas"), c), corpo));
    }

    private long vendasDaChave(String chave) {
        return pedidoRepository.findByChaveVenda(chave).isPresent() ? 1 : 0;
    }

    // ── Busca ─────────────────────────────────────────────────────────────

    @Test
    void buscaPorNomeSemAcentoEPorCodigo() throws Exception {
        String base = "/api/pdv/produtos?barracaId=" + quiosque.getId();
        assertThat(corpo(chamar(auth(GET(base), loja)))).hasSize(2);
        JsonNode porNome = corpo(chamar(auth(GET(base + "&busca=agua"), loja)));
        assertThat(porNome).hasSize(1);
        assertThat(porNome.get(0).path("nome").asText()).isEqualTo("Água Mineral 500ml");
        assertThat(porNome.get(0).path("precoVenda").decimalValue()).isEqualByComparingTo("2.25");
        assertThat(porNome.get(0).path("disponivel").decimalValue()).isEqualByComparingTo("5");

        JsonNode porCodigo = corpo(chamar(auth(GET(base + "&busca=" + cerveja.getCodigo().toLowerCase()), loja)));
        assertThat(porCodigo).hasSize(1);
        assertThat(porCodigo.get(0).path("id").asLong()).isEqualTo(cerveja.getId());
        assertThat(corpo(chamar(auth(GET(base + "&busca=inexistente"), loja)))).isEmpty();
    }

    @Test
    void buscaMostraDisponivelComoOMenorEntreQuiosqueEProdutoEOcultaInativos() throws Exception {
        Produto limitado = novoProduto(loja.empresa(), "2", "1.00");
        alocar(quiosque, limitado, "8");          // alocação maior que o estoque total
        Produto inativo = novoProduto(loja.empresa(), "5", "1.00");
        inativo.setAtivo(0);
        produtoRepository.save(inativo);
        alocar(quiosque, inativo, "5");

        JsonNode lista = corpo(chamar(auth(GET("/api/pdv/produtos?barracaId=" + quiosque.getId()), loja)));
        assertThat(lista.toString()).doesNotContain(inativo.getNome());
        for (JsonNode p : lista) {
            if (p.path("id").asLong() == limitado.getId()) {
                assertThat(p.path("disponivel").decimalValue()).isEqualByComparingTo("2");
            }
        }
    }

    @Test
    void buscaExigeLoginEQuiosqueDaPropriaEmpresa() throws Exception {
        Conta outra = novoFornecedor();
        String url = "/api/pdv/produtos?barracaId=" + quiosque.getId();
        assertThat(status(GET(url))).isEqualTo(401);
        assertThat(status(auth(GET(url), outra))).isEqualTo(403);
        assertThat(status(auth(GET("/api/pdv/produtos?barracaId=987654321"), loja))).isEqualTo(404);
        assertThat(status(auth(GET("/api/pdv/produtos"), loja))).isEqualTo(400);
    }

    // ── Venda, totais e troco ─────────────────────────────────────────────

    @Test
    void vendaUsaPrecosDoCadastroIgnorandoPrecoETotalDaRequisicao() throws Exception {
        Map<String, Object> v = venda("pix", cerveja.getId(), 3, agua.getId(), 2);
        v.put("total", 0.01);
        v.put("valorTotal", 0.01);
        ((List<Map<String, Object>>) v.get("itens")).forEach(i -> { i.put("precoUnitario", 0.01); i.put("subtotal", 0.01); });

        MvcResult r = vender(loja, v);
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        JsonNode venda = corpo(r);
        assertThat(venda.path("total").decimalValue()).isEqualByComparingTo("18.00");   // 3×4,50 + 2×2,25
        assertThat(venda.path("numero").asText()).startsWith("PDV-");
        assertThat(venda.path("formaPagamento").asText()).isEqualTo("pix");
        assertThat(venda.path("troco").isNull()).isTrue();
        JsonNode itens = venda.path("itens");
        assertThat(itens).hasSize(2);
        itens.forEach(i -> assertThat(i.path("precoUnitario").decimalValue()).isGreaterThan(new BigDecimal("1")));

        Pedido pedido = pedidoRepository.findById(venda.path("id").asLong()).orElseThrow();
        assertThat(pedido.getTipo()).isEqualTo("pdv");
        assertThat(pedido.getStatus()).isEqualTo("concluido");
        assertThat(pedido.getOperador().getId()).isEqualTo(loja.usuarioId());
        assertThat(pedido.getValorTotal()).isEqualByComparingTo("18.00");
        List<Pagamento> pagamentos = pagamentoRepository.findByPedidoId(pedido.getId());
        assertThat(pagamentos).hasSize(1);
        assertThat(pagamentos.get(0).getStatus()).isEqualTo("pendente");   // só registra a forma, sem aprovar
        assertThat(pagamentos.get(0).getValor()).isEqualByComparingTo("18.00");

        assertThat(noQuiosque(cerveja)).isEqualByComparingTo("7");
        assertThat(noProduto(cerveja)).isEqualByComparingTo("27");
        assertThat(noQuiosque(agua)).isEqualByComparingTo("3");
        assertThat(noProduto(agua)).isEqualByComparingTo("3");
    }

    @Test
    void dinheiroCalculaTrocoNoServidor() throws Exception {
        Map<String, Object> v = venda("dinheiro", cerveja.getId(), 3);   // 13,50
        v.put("valorRecebido", 20);
        v.put("troco", 100);
        JsonNode venda = corpo(vender(loja, v));
        assertThat(venda.path("total").decimalValue()).isEqualByComparingTo("13.50");
        assertThat(venda.path("valorRecebido").decimalValue()).isEqualByComparingTo("20.00");
        assertThat(venda.path("troco").decimalValue()).isEqualByComparingTo("6.50");

        Map<String, Object> exato = venda("dinheiro", agua.getId(), 2);  // 4,50
        exato.put("valorRecebido", 4.5);
        assertThat(corpo(vender(loja, exato)).path("troco").decimalValue()).isEqualByComparingTo("0");
    }

    @Test
    void dinheiroInsuficienteOuSemValorRecebidoNaoRegistraNada() throws Exception {
        Map<String, Object> v = venda("dinheiro", cerveja.getId(), 3);
        v.put("valorRecebido", 13.49);
        assertThat(vender(loja, v).getResponse().getStatus()).isEqualTo(400);
        assertThat(vendasDaChave((String) v.get("chaveVenda"))).isZero();

        Map<String, Object> semValor = venda("dinheiro", cerveja.getId(), 1);
        assertThat(vender(loja, semValor).getResponse().getStatus()).isEqualTo(400);
        assertThat(noQuiosque(cerveja)).isEqualByComparingTo("10");
        assertThat(noProduto(cerveja)).isEqualByComparingTo("30");
    }

    @Test
    void itensRepetidosViramUmSoEQuantidadeDecimalFunciona() throws Exception {
        JsonNode venda = corpo(vender(loja, venda("debito", cerveja.getId(), 1, cerveja.getId(), 2, agua.getId(), 0.5)));
        assertThat(venda.path("itens")).hasSize(2);
        assertThat(venda.path("total").decimalValue()).isEqualByComparingTo("14.63"); // 13,50 + 1,125 → 1,13
        assertThat(noQuiosque(cerveja)).isEqualByComparingTo("7");
        assertThat(noQuiosque(agua)).isEqualByComparingTo("4.5");
    }

    @Test
    void clienteOpcionalEhGravadoEDocumentoSaiMascarado() throws Exception {
        Map<String, Object> v = venda("pix", agua.getId(), 1);
        v.put("clienteNome", "  Maria Souza ");
        v.put("clienteDocumento", "123.456.789-09");
        JsonNode venda = corpo(vender(loja, v));
        assertThat(venda.path("clienteNome").asText()).isEqualTo("Maria Souza");
        assertThat(venda.path("clienteDocumento").asText()).isEqualTo("***.***.***-09");
        Pedido p = pedidoRepository.findById(venda.path("id").asLong()).orElseThrow();
        assertThat(p.getClienteDocumento()).isEqualTo("12345678909");

        JsonNode anonima = corpo(vender(loja, venda("pix", agua.getId(), 1)));
        assertThat(anonima.path("clienteNome").isNull()).isTrue();
    }

    // ── Validação e estoque ───────────────────────────────────────────────

    @Test
    void entradasInvalidasRespondem400SemGravar() throws Exception {
        List<Map<String, Object>> invalidas = new ArrayList<>();
        Map<String, Object> v;
        v = venda("pix"); invalidas.add(v);                                              // sem itens
        v = venda("pix", cerveja.getId(), 0); invalidas.add(v);                          // quantidade zero
        v = venda("pix", cerveja.getId(), -1); invalidas.add(v);                         // negativa
        v = venda("pix", cerveja.getId(), 1.0001); invalidas.add(v);                     // 4 casas
        v = venda("boleto", cerveja.getId(), 1); invalidas.add(v);                       // forma inexistente
        v = venda("pix", cerveja.getId(), 1); v.put("chaveVenda", "x"); invalidas.add(v);  // chave inválida
        v = venda("pix", cerveja.getId(), 1); v.put("clienteDocumento", "123"); invalidas.add(v);
        v = venda("pix", cerveja.getId(), 1); v.put("clienteNome", "x".repeat(101)); invalidas.add(v);
        v = venda("pix", cerveja.getId(), 1); v.remove("barracaId"); invalidas.add(v);
        v = venda("pix", cerveja.getId(), 1); v.put("valorRecebido", -5); invalidas.add(v);
        for (Map<String, Object> corpo : invalidas) {
            assertThat(vender(loja, corpo).getResponse().getStatus()).as(corpo.toString()).isEqualTo(400);
        }
        assertThat(noQuiosque(cerveja)).isEqualByComparingTo("10");
        assertThat(pedidoRepository.findByBarracaId(quiosque.getId())).isEmpty();
    }

    @Test
    void estoqueInsuficienteNoQuiosqueOuNoProdutoEhRecusado() throws Exception {
        assertThat(vender(loja, venda("pix", cerveja.getId(), 11)).getResponse().getStatus()).isEqualTo(400); // quiosque tem 10
        Produto pouco = novoProduto(loja.empresa(), "1", "1.00");
        alocar(quiosque, pouco, "5");                                                                       // produto só tem 1
        MvcResult r = vender(loja, venda("pix", pouco.getId(), 2));
        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(corpo(r).path("erro").asText()).contains("Estoque insuficiente");
        assertThat(noQuiosque(cerveja)).isEqualByComparingTo("10");
        assertThat(noProduto(pouco)).isEqualByComparingTo("1");
    }

    @Test
    void falhaNoMeioDaVendaDesfazTudo() throws Exception {
        long pagamentos = pagamentoRepository.count();
        Map<String, Object> v = venda("pix", cerveja.getId(), 4, agua.getId(), 6);   // água só tem 5
        assertThat(vender(loja, v).getResponse().getStatus()).isEqualTo(400);
        assertThat(noQuiosque(cerveja)).isEqualByComparingTo("10");
        assertThat(noProduto(cerveja)).isEqualByComparingTo("30");
        assertThat(vendasDaChave((String) v.get("chaveVenda"))).isZero();
        assertThat(pedidoRepository.findByBarracaId(quiosque.getId())).isEmpty();
        assertThat(pagamentoRepository.count()).isEqualTo(pagamentos);
    }

    @Test
    void produtoForaDoQuiosqueOuDeOutraEmpresaEhRecusadoSemBaixaParcial() throws Exception {
        Produto semAlocacao = novoProduto(loja.empresa(), "9", "1.00");
        assertThat(vender(loja, venda("pix", cerveja.getId(), 1, semAlocacao.getId(), 1)).getResponse().getStatus()).isEqualTo(400);

        Conta outra = novoFornecedor();
        Produto alheio = novoProduto(outra.empresa(), "9", "0.10");
        assertThat(vender(loja, venda("pix", cerveja.getId(), 1, alheio.getId(), 1)).getResponse().getStatus()).isEqualTo(403);
        assertThat(vender(loja, venda("pix", 987654321L, 1)).getResponse().getStatus()).isEqualTo(400);

        assertThat(noQuiosque(cerveja)).isEqualByComparingTo("10");
        assertThat(noProduto(alheio)).isEqualByComparingTo("9");
    }

    @Test
    void quiosqueDesativadoNaoVende() throws Exception {
        quiosque.setAtiva(0);
        barracaRepository.save(quiosque);
        assertThat(vender(loja, venda("pix", cerveja.getId(), 1)).getResponse().getStatus()).isEqualTo(400);
        assertThat(noQuiosque(cerveja)).isEqualByComparingTo("10");
    }

    // ── Isolamento entre empresas ─────────────────────────────────────────

    @Test
    void naoVendeNemConsultaVendasDeOutraEmpresa() throws Exception {
        Conta outra = novoFornecedor();
        assertThat(vender(outra, venda("pix", cerveja.getId(), 1)).getResponse().getStatus()).isEqualTo(403);
        assertThat(status(comCorpo(POST("/api/pdv/vendas"), venda("pix", cerveja.getId(), 1)))).isEqualTo(401);
        assertThat(noQuiosque(cerveja)).isEqualByComparingTo("10");

        long id = corpo(vender(loja, venda("pix", cerveja.getId(), 1))).path("id").asLong();
        assertThat(status(auth(GET("/api/pdv/vendas/" + id), loja))).isEqualTo(200);
        assertThat(status(auth(GET("/api/pdv/vendas/" + id), outra))).isEqualTo(403);
    }

    @Test
    void chaveDeVendaDeOutraEmpresaNaoVazaAVenda() throws Exception {
        Map<String, Object> v = venda("pix", cerveja.getId(), 1);
        vender(loja, v);
        Conta outra = novoFornecedor();
        Barraca deOutra = novaBarraca(outra);
        Produto p = novoProduto(outra.empresa(), "5", "1.00");
        alocar(deOutra, p, "5");
        Map<String, Object> mesmaChave = new LinkedHashMap<>(v);
        mesmaChave.put("barracaId", deOutra.getId());
        mesmaChave.put("itens", List.of(Map.of("produtoId", p.getId(), "quantidade", 1)));
        assertThat(vender(outra, mesmaChave).getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    void vendaFinalizadaNaoPodeSerAlteradaPelosEndpointsGenericos() throws Exception {
        long id = corpo(vender(loja, venda("pix", cerveja.getId(), 2))).path("id").asLong();
        assertThat(status(comCorpo(auth(PATCH("/api/pedidos/" + id + "/status"), loja), Map.of("status", "aberto")))).isEqualTo(409);
        assertThat(status(auth(DELETE("/api/pedidos/" + id), loja))).isEqualTo(409);
        assertThat(status(comCorpo(auth(POST("/api/itens-pedido"), loja), Map.of(
                "pedido", Map.of("id", id), "produto", Map.of("id", cerveja.getId()), "quantidade", 1, "precoUnitario", 0)))).isEqualTo(409);
        assertThat(pedidoRepository.findById(id).orElseThrow().getStatus()).isEqualTo("concluido");
        assertThat(pedidoRepository.findById(id).orElseThrow().getValorTotal()).isEqualByComparingTo("9.00");
    }

    @Test
    void consultarPedidoQueNaoEhVendaDoPdvResponde404() throws Exception {
        Pedido avulso = new Pedido();
        avulso.setTipo("pdv"); avulso.setBarraca(quiosque); avulso.setOperador(loja.usuario());
        avulso.setStatus("aberto"); avulso.setValorTotal(BigDecimal.ZERO);
        avulso = pedidoRepository.save(avulso);
        assertThat(status(auth(GET("/api/pdv/vendas/" + avulso.getId()), loja))).isEqualTo(404);
    }

    // ── Cliques repetidos e concorrência ──────────────────────────────────

    @Test
    void reenviarAMesmaVendaNaoDuplicaNemBaixaEstoqueDeNovo() throws Exception {
        Map<String, Object> v = venda("pix", cerveja.getId(), 2);
        MvcResult primeira = vender(loja, v);
        MvcResult segunda = vender(loja, v);
        assertThat(primeira.getResponse().getStatus()).isEqualTo(201);
        assertThat(segunda.getResponse().getStatus()).isEqualTo(200);
        assertThat(corpo(segunda).path("id").asLong()).isEqualTo(corpo(primeira).path("id").asLong());
        assertThat(noQuiosque(cerveja)).isEqualByComparingTo("8");
        assertThat(pedidoRepository.findByBarracaId(quiosque.getId())).hasSize(1);
    }

    @Test
    void cliquesSimultaneosComAMesmaChaveRegistramUmaVendaSo() throws Exception {
        Map<String, Object> v = venda("pix", cerveja.getId(), 1);
        List<MvcResult> respostas = emParalelo(8, () -> vender(loja, v));

        Set<Long> ids = new HashSet<>();
        for (MvcResult r : respostas) {
            assertThat(r.getResponse().getStatus()).isIn(200, 201);
            ids.add(corpo(r).path("id").asLong());
        }
        assertThat(ids).hasSize(1);
        assertThat(respostas.stream().filter(r -> r.getResponse().getStatus() == 201)).hasSize(1);
        assertThat(pedidoRepository.findByBarracaId(quiosque.getId())).hasSize(1);
        assertThat(noQuiosque(cerveja)).isEqualByComparingTo("9");
        assertThat(noProduto(cerveja)).isEqualByComparingTo("29");
    }

    @Test
    void vendasSimultaneasNaoDeixamOEstoqueNegativo() throws Exception {
        // água: 5 unidades; 8 caixas tentam vender 1 cada ao mesmo tempo
        List<MvcResult> respostas = emParalelo(8, () -> vender(loja, venda("pix", agua.getId(), 1)));
        long ok = respostas.stream().filter(r -> r.getResponse().getStatus() == 201).count();
        long recusadas = respostas.stream().filter(r -> r.getResponse().getStatus() == 400).count();
        assertThat(ok).isEqualTo(5);
        assertThat(recusadas).isEqualTo(3);
        assertThat(noQuiosque(agua)).isEqualByComparingTo("0");
        assertThat(noProduto(agua)).isEqualByComparingTo("0");
        assertThat(pedidoRepository.findByBarracaId(quiosque.getId())).hasSize(5);
    }

    private List<MvcResult> emParalelo(int n, Callable<MvcResult> tarefa) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<MvcResult>> futuros = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                futuros.add(pool.submit(() -> {
                    largada.await();
                    return tarefa.call();
                }));
            }
            largada.countDown();
            List<MvcResult> resultados = new ArrayList<>();
            for (Future<MvcResult> f : futuros) resultados.add(f.get(30, TimeUnit.SECONDS));
            return resultados;
        } finally {
            pool.shutdownNow();
        }
    }
}
