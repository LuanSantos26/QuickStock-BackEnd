package com.stock4event.backend.integracao;

import com.stock4event.backend.entity.*;
import com.stock4event.backend.support.IntegracaoBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Entradas inválidas (400), duplicidade (409) e referências inexistentes ou inconsistentes, sem gravar nada. */
class ValidacaoEIntegridadeIT extends IntegracaoBase {

    private Conta cliente;
    private Conta fornecedor;
    private Produto produto;
    private EnderecoEntrega endereco;

    @BeforeEach
    void dados() {
        cliente = novoCliente();
        fornecedor = novoFornecedor();
        produto = novoProduto(fornecedor.empresa(), "10", "5.00");
        endereco = novoEndereco(cliente.empresa());
    }

    private Map<String, Object> pedidoValido() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("empresaFornecedoraId", fornecedor.empresaId());
        m.put("metodoPagamento", "pix");
        m.put("enderecoEntregaId", endereco.getId());
        m.put("taxaEntrega", 7);
        m.put("itens", List.of(Map.of("produtoId", produto.getId(), "quantidade", 1)));
        return m;
    }

    private void assertNenhumPedidoEEstoqueIntacto() {
        assertThat(solicitacaoRepository.findByEmpresaCompradoraIdOrderByCriadoEmDesc(cliente.empresaId())).isEmpty();
        assertThat(produtoRepository.findById(produto.getId()).orElseThrow().getEstoque()).isEqualByComparingTo("10");
    }

    // ── Validação de entrada ──────────────────────────────────────────────

    @Test
    void produtoComCamposInvalidosResponde400ENaoGrava() throws Exception {
        long antes = produtoRepository.findByEmpresaIdAndAtivo(cliente.empresaId(), 1).size();
        List<Map<String, Object>> invalidos = List.of(
                Map.of("precoVenda", 1, "unidade", "UN"),                                    // nome ausente
                Map.of("nome", "  ", "precoVenda", 1, "unidade", "UN"),                      // nome vazio
                Map.of("nome", "x".repeat(301), "precoVenda", 1, "unidade", "UN"),           // nome longo
                Map.of("nome", "P", "precoVenda", -1, "unidade", "UN"),                      // preço negativo
                Map.of("nome", "P", "precoVenda", 1, "unidade", "UN", "estoque", -5),        // estoque negativo
                Map.of("nome", "P", "precoVenda", "abc", "unidade", "UN"),                   // formato inválido
                Map.of("nome", "P", "precoVenda", 1));                                       // unidade ausente
        for (Map<String, Object> corpo : invalidos) {
            assertThat(status(comCorpo(auth(POST("/api/produtos"), cliente), corpo))).as(corpo.toString()).isEqualTo(400);
        }
        assertThat(produtoRepository.findByEmpresaIdAndAtivo(cliente.empresaId(), 1)).hasSize((int) antes);
    }

    @Test
    void jsonMalformadoResponde400() throws Exception {
        assertThat(status(comCorpo(auth(POST("/api/produtos"), cliente), "{\"nome\": "))).isEqualTo(400);
    }

    @Test
    void enderecoComCepOuUfInvalidoResponde400() throws Exception {
        Map<String, Object> base = new LinkedHashMap<>(Map.of("apelido", "Depósito", "logradouro", "Rua", "numero", "1",
                "bairro", "B", "cidade", "C", "uf", "PE", "cep", "55000-000"));
        base.put("cep", "123");
        assertThat(status(comCorpo(auth(POST("/api/enderecos"), cliente), base))).isEqualTo(400);
        base.put("cep", "55000-000"); base.put("uf", "PER");
        assertThat(status(comCorpo(auth(POST("/api/enderecos"), cliente), base))).isEqualTo(400);
        base.put("uf", "PE"); base.remove("logradouro");
        assertThat(status(comCorpo(auth(POST("/api/enderecos"), cliente), base))).isEqualTo(400);
        assertThat(enderecoRepository.findByEmpresaIdOrderByPrincipalDescApelidoAsc(cliente.empresaId())).hasSize(1);
    }

    @Test
    void cartaoComTipoOuValidadeInvalidosResponde400() throws Exception {
        Map<String, Object> c = new LinkedHashMap<>(Map.of("tipo", "boleto", "bandeira", "Visa",
                "ultimosDigitos", "1234", "validade", "12/30", "titular", "Ana"));
        assertThat(status(comCorpo(auth(POST("/api/cartoes-pagamento"), cliente), c))).isEqualTo(400);
        c.put("tipo", "credito"); c.put("validade", "1230");
        assertThat(status(comCorpo(auth(POST("/api/cartoes-pagamento"), cliente), c))).isEqualTo(400);
        c.put("validade", "12/30"); c.put("ultimosDigitos", "12a");
        assertThat(status(comCorpo(auth(POST("/api/cartoes-pagamento"), cliente), c))).isEqualTo(400);
        assertThat(cartaoRepository.findByEmpresaIdOrderByIdDesc(cliente.empresaId())).isEmpty();
    }

    @Test
    void pedidoComDadosInvalidosResponde400ENaoMexeNoEstoque() throws Exception {
        Map<String, Object> p = pedidoValido();
        p.put("metodoPagamento", "boleto");
        assertThat(status(comCorpo(auth(POST("/api/solicitacoes-compra"), cliente), p))).isEqualTo(400);

        p = pedidoValido(); p.put("taxaEntrega", -7);
        assertThat(status(comCorpo(auth(POST("/api/solicitacoes-compra"), cliente), p))).isEqualTo(400);

        p = pedidoValido(); p.put("itens", List.of());
        assertThat(status(comCorpo(auth(POST("/api/solicitacoes-compra"), cliente), p))).isEqualTo(400);

        p = pedidoValido(); p.put("itens", List.of(Map.of("produtoId", produto.getId(), "quantidade", 0)));
        assertThat(status(comCorpo(auth(POST("/api/solicitacoes-compra"), cliente), p))).isEqualTo(400);

        p = pedidoValido(); p.put("itens", List.of(Map.of("produtoId", produto.getId(), "quantidade", 11)));
        assertThat(status(comCorpo(auth(POST("/api/solicitacoes-compra"), cliente), p))).isEqualTo(400);

        p = pedidoValido(); p.remove("enderecoEntregaId");
        assertThat(status(comCorpo(auth(POST("/api/solicitacoes-compra"), cliente), p))).isEqualTo(400);

        assertNenhumPedidoEEstoqueIntacto();
    }

    @Test
    void atualizacaoDoProprioUsuarioValidaCampos() throws Exception {
        String url = "/api/usuarios/" + cliente.usuarioId();
        assertThat(status(comCorpo(auth(PUT(url), cliente), Map.of("email", "invalido")))).isEqualTo(400);
        assertThat(status(comCorpo(auth(PUT(url), cliente), Map.of("senha", "123")))).isEqualTo(400);
        assertThat(status(comCorpo(auth(PUT(url), cliente), Map.of("nome", "   ")))).isEqualTo(400);
        assertThat(usuarioRepository.findById(cliente.usuarioId()).orElseThrow().getEmail()).isEqualTo(cliente.usuario().getEmail());
    }

    @Test
    void cadastroInvalidoResponde400ENaoCriaEmpresa() throws Exception {
        long empresas = empresaRepository.count();
        assertThat(status(comCorpo(POST("/api/cadastro"), Map.of("empresa", Map.of("nome", "X", "cnpj", "1"))))).isEqualTo(400);
        assertThat(status(comCorpo(POST("/api/cadastro"), Map.of("perfil", "Administrador",
                "empresa", Map.of("nome", "X", "cnpj", "88.888.888/0001-88"),
                "usuario", Map.of("nome", "Y", "email", "y" + seq() + "@teste.local", "senha", "senha123"))))).isEqualTo(400);
        assertThat(empresaRepository.count()).isEqualTo(empresas);
    }

    @Test
    void ajusteDeEstoqueEPagamentoRecusamValoresInvalidos() throws Exception {
        Produto meu = novoProduto(cliente.empresa(), "5", "1.00");
        Barraca barraca = novaBarraca(cliente);
        EstoqueBarraca e = new EstoqueBarraca();
        e.setBarraca(barraca); e.setProduto(meu); e.setQuantidade(new BigDecimal("2"));
        e = estoqueBarracaRepository.save(e);
        String url = "/api/estoque-barraca/" + e.getId() + "/quantidade";
        assertThat(status(comCorpo(auth(PATCH(url), cliente), Map.of("quantidade", "abc")))).isEqualTo(400);
        assertThat(status(comCorpo(auth(PATCH(url), cliente), Map.of("quantidade", -1)))).isEqualTo(400);
        assertThat(estoqueBarracaRepository.findById(e.getId()).orElseThrow().getQuantidade()).isEqualByComparingTo("2");

        Pedido pdv = new Pedido();
        pdv.setTipo("pdv"); pdv.setBarraca(barraca); pdv.setOperador(cliente.usuario());
        pdv.setStatus("aberto"); pdv.setValorTotal(BigDecimal.ZERO);
        pdv = pedidoRepository.save(pdv);
        Pagamento pg = new Pagamento();
        pg.setPedido(pdv); pg.setMetodo("dinheiro"); pg.setValor(BigDecimal.ONE); pg.setStatus("pendente");
        pg = pagamentoRepository.save(pg);
        assertThat(status(comCorpo(auth(PATCH("/api/pagamentos/" + pg.getId() + "/status"), cliente),
                Map.of("status", "pago-por-mim")))).isEqualTo(400);
        assertThat(status(comCorpo(auth(POST("/api/pagamentos"), cliente),
                Map.of("pedido", Map.of("id", pdv.getId()), "metodo", "pix", "valor", 0)))).isEqualTo(400);
        assertThat(pagamentoRepository.findById(pg.getId()).orElseThrow().getStatus()).isEqualTo("pendente");
    }

    @Test
    void parametroComTipoErradoResponde400() throws Exception {
        assertThat(status(auth(GET("/api/enderecos?empresaId=abc"), cliente))).isEqualTo(400);
        assertThat(status(auth(GET("/api/produtos/abc"), cliente))).isEqualTo(400);
    }

    // ── Integridade ───────────────────────────────────────────────────────

    @Test
    void cadastroComEmailOuCnpjDuplicadoResponde409() throws Exception {
        long empresas = empresaRepository.count();
        assertThat(status(comCorpo(POST("/api/cadastro"), Map.of(
                "empresa", Map.of("nome", "Duplicada", "cnpj", "55.555.555/0001-" + String.format("%02d", seq() % 100)),
                "usuario", Map.of("nome", "Dup", "email", cliente.usuario().getEmail(), "senha", "senha123"))))).isEqualTo(409);
        assertThat(status(comCorpo(POST("/api/cadastro"), Map.of(
                "empresa", Map.of("nome", "Duplicada", "cnpj", cliente.empresa().getCnpj()),
                "usuario", Map.of("nome", "Dup", "email", "novo" + seq() + "@teste.local", "senha", "senha123"))))).isEqualTo(409);
        assertThat(empresaRepository.count()).isEqualTo(empresas);
    }

    @Test
    void alterarParaCnpjOuEmailJaUsadoResponde409() throws Exception {
        assertThat(status(comCorpo(auth(PUT("/api/empresas/" + cliente.empresaId()), cliente),
                Map.of("nome", "Minha", "cnpj", fornecedor.empresa().getCnpj())))).isEqualTo(409);
        assertThat(empresaRepository.findById(cliente.empresaId()).orElseThrow().getCnpj()).isEqualTo(cliente.empresa().getCnpj());

        assertThat(status(comCorpo(auth(PUT("/api/usuarios/" + cliente.usuarioId()), cliente),
                Map.of("email", fornecedor.usuario().getEmail())))).isEqualTo(409);
        assertThat(usuarioRepository.findById(cliente.usuarioId()).orElseThrow().getEmail()).isEqualTo(cliente.usuario().getEmail());
    }

    @Test
    void formaDePagamentoEEstoqueDeQuiosqueDuplicadosSaoRecusados() throws Exception {
        Map<String, Object> forma = Map.of("tipo", "pix", "apelido", "Meu PIX");
        assertThat(status(comCorpo(auth(POST("/api/formas-pagamento"), cliente), forma))).isEqualTo(201);
        // regra existente no FormaPagamentoSalvaService: duplicidade responde 400
        assertThat(status(comCorpo(auth(POST("/api/formas-pagamento"), cliente), forma))).isEqualTo(400);
        assertThat(formaRepository.findByEmpresaIdOrderByPrincipalDescIdAsc(cliente.empresaId())).hasSize(1);

        Produto meu = novoProduto(cliente.empresa(), "5", "1.00");
        Barraca barraca = novaBarraca(cliente);
        Map<String, Object> linha = Map.of("barraca", Map.of("id", barraca.getId()), "produto", Map.of("id", meu.getId()), "quantidade", 1);
        assertThat(status(comCorpo(auth(POST("/api/estoque-barraca"), cliente), linha))).isEqualTo(201);
        assertThat(status(comCorpo(auth(POST("/api/estoque-barraca"), cliente), linha))).isEqualTo(409);
        assertThat(estoqueBarracaRepository.findByBarracaId(barraca.getId())).hasSize(1);
    }

    @Test
    void referenciasInexistentesSaoRecusadas() throws Exception {
        Map<String, Object> p = pedidoValido();
        p.put("itens", List.of(Map.of("produtoId", 987654321L, "quantidade", 1)));
        assertThat(status(comCorpo(auth(POST("/api/solicitacoes-compra"), cliente), p))).isEqualTo(400);
        p = pedidoValido(); p.put("empresaFornecedoraId", 987654321L);
        assertThat(status(comCorpo(auth(POST("/api/solicitacoes-compra"), cliente), p))).isEqualTo(400);
        p = pedidoValido(); p.put("enderecoEntregaId", 987654321L);
        assertThat(status(comCorpo(auth(POST("/api/solicitacoes-compra"), cliente), p))).isEqualTo(400);

        assertThat(status(comCorpo(auth(POST("/api/barracas"), cliente),
                Map.of("nome", "Sem dono", "responsavelId", 987654321L)))).isEqualTo(400);
        assertThat(status(comCorpo(auth(POST("/api/estoque-barraca"), cliente),
                Map.of("barraca", Map.of("id", 987654321L), "produto", Map.of("id", produto.getId()), "quantidade", 1)))).isEqualTo(404);
        assertNenhumPedidoEEstoqueIntacto();
    }

    @Test
    void relacionamentosInconsistentesSaoRecusados() throws Exception {
        Conta outroFornecedor = novoFornecedor();
        Produto deOutro = novoProduto(outroFornecedor.empresa(), "10", "1.00");

        Map<String, Object> p = pedidoValido();
        p.put("itens", List.of(Map.of("produtoId", deOutro.getId(), "quantidade", 1))); // produto não é do fornecedor escolhido
        assertThat(status(comCorpo(auth(POST("/api/solicitacoes-compra"), cliente), p))).isEqualTo(400);

        Conta outroCliente = novoCliente();
        p = pedidoValido(); p.put("empresaFornecedoraId", outroCliente.empresaId()); // empresa não é fornecedora
        assertThat(status(comCorpo(auth(POST("/api/solicitacoes-compra"), cliente), p))).isEqualTo(400);

        EnderecoEntrega endFornecedor = novoEndereco(fornecedor.empresa());
        Map<String, Object> proprio = pedidoValido();               // fornecedor comprando de si mesmo
        proprio.put("enderecoEntregaId", endFornecedor.getId());
        assertThat(status(comCorpo(auth(POST("/api/solicitacoes-compra"), fornecedor), proprio))).isEqualTo(400);

        // quiosque com produto de outra empresa ou responsável de outra empresa
        assertThat(status(comCorpo(auth(POST("/api/barracas"), cliente), Map.of("nome", "Q", "responsavelId", cliente.usuarioId(),
                "itens", List.of(Map.of("produtoId", produto.getId(), "quantidade", 1)))))).isEqualTo(403);
        assertThat(status(comCorpo(auth(POST("/api/barracas"), cliente),
                Map.of("nome", "Q", "responsavelId", fornecedor.usuarioId())))).isEqualTo(403);

        assertNenhumPedidoEEstoqueIntacto();
        assertThat(produtoRepository.findById(deOutro.getId()).orElseThrow().getEstoque()).isEqualByComparingTo("10");
    }

    @Test
    void excluirEventoComQuiosqueVinculadoResponde409EMantemOsDados() throws Exception {
        Barraca barraca = novaBarraca(cliente);
        Long eventoId = barraca.getEvento().getId();
        assertThat(status(auth(DELETE("/api/eventos/" + eventoId), cliente))).isEqualTo(409);
        assertThat(eventoRepository.existsById(eventoId)).isTrue();
        assertThat(barracaRepository.existsById(barraca.getId())).isTrue();
    }

    @Test
    void eventoSemDatasOuNomeResponde400() throws Exception {
        long antes = eventoRepository.findByEmpresaId(cliente.empresaId()).size();
        assertThat(status(comCorpo(auth(POST("/api/eventos"), cliente), Map.of("nome", "Festa")))).isEqualTo(400);
        assertThat(status(comCorpo(auth(POST("/api/eventos"), cliente),
                Map.of("nome", "", "dataInicio", LocalDate.now().toString(), "dataFim", LocalDate.now().toString())))).isEqualTo(400);
        assertThat(eventoRepository.findByEmpresaId(cliente.empresaId())).hasSize((int) antes);
    }
}
