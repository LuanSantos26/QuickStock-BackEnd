package com.stock4event.backend.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock4event.backend.entity.*;
import com.stock4event.backend.repository.*;
import com.stock4event.backend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Base dos testes de integração: sobe a aplicação inteira (com o Spring Security real)
 * contra o banco exclusivo stock4event_test e cria dados fictícios isolados por teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegracaoBase {

    static {
        BancoDeTeste.garantir();
    }

    private static final AtomicInteger SEQ = new AtomicInteger(100);
    protected static final String SENHA = "senha123";

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;
    @Autowired protected JwtService jwtService;
    @Autowired protected EmpresaRepository empresaRepository;
    @Autowired protected UsuarioRepository usuarioRepository;
    @Autowired protected PerfilRepository perfilRepository;
    @Autowired protected ProdutoRepository produtoRepository;
    @Autowired protected EnderecoEntregaRepository enderecoRepository;
    @Autowired protected CartaoPagamentoSalvoRepository cartaoRepository;
    @Autowired protected ChavePixSalvaRepository chavePixRepository;
    @Autowired protected FormaPagamentoSalvaRepository formaRepository;
    @Autowired protected EventoRepository eventoRepository;
    @Autowired protected BarracaRepository barracaRepository;
    @Autowired protected EstoqueBarracaRepository estoqueBarracaRepository;
    @Autowired protected PedidoRepository pedidoRepository;
    @Autowired protected PagamentoRepository pagamentoRepository;
    @Autowired protected SolicitacaoCompraRepository solicitacaoRepository;
    @Autowired protected CaminhoneiroRepository caminhoneiroRepository;

    /** Empresa + usuário + token válido. */
    public record Conta(Empresa empresa, Usuario usuario, String token) {
        public Long empresaId() { return empresa.getId(); }
        public Long usuarioId() { return usuario.getId(); }
    }

    protected int seq() {
        return SEQ.incrementAndGet();
    }

    protected Perfil perfilAdmin() {
        return perfilRepository.findByNomeIgnoreCase("Admin").orElseGet(() -> {
            Perfil p = new Perfil();
            p.setNome("Admin");
            p.setDescricao("Administrador da empresa");
            return perfilRepository.save(p);
        });
    }

    protected Empresa novaEmpresa(String tipo) {
        int n = seq();
        Empresa e = new Empresa();
        e.setNome("Empresa Teste " + n);
        e.setCnpj(String.format("99.%03d.%03d/0001-%02d", n / 1000, n % 1000, n % 100));
        e.setTelefone("(81) 90000-" + String.format("%04d", n));
        e.setTipo(tipo);
        return empresaRepository.save(e);
    }

    protected Usuario novoUsuario(Empresa empresa) {
        int n = seq();
        Usuario u = new Usuario();
        u.setNome("Usuário Teste " + n);
        u.setEmail("usuario" + n + "@teste.local");
        u.setSenhaHash(BCrypt.hashpw(SENHA, BCrypt.gensalt(4)));
        u.setPerfil(perfilAdmin());
        u.setEmpresa(empresa);
        u.setAtivo(1);
        u.setPerfilUso("ClienteFornecedor");
        return usuarioRepository.save(u);
    }

    protected Conta novaConta(String tipo) {
        Empresa e = novaEmpresa(tipo);
        Usuario u = novoUsuario(e);
        return new Conta(e, u, jwtService.generateToken(u));
    }

    protected Conta novoCliente() {
        return novaConta("CLIENTE");
    }

    protected Conta novoFornecedor() {
        return novaConta("DISTRIBUIDOR");
    }

    protected Produto novoProduto(Empresa empresa, String estoque, String preco) {
        int n = seq();
        Produto p = new Produto();
        p.setEmpresa(empresa);
        p.setNome("Produto Teste " + n);
        p.setPrecoVenda(new BigDecimal(preco));
        p.setUnidade("UN");
        p.setDescricao("Produto fictício");
        p.setEstoque(new BigDecimal(estoque));
        p.setAtivo(1);
        p.setCodigo("TST-" + n);
        return produtoRepository.save(p);
    }

    protected EnderecoEntrega novoEndereco(Empresa empresa) {
        EnderecoEntrega e = new EnderecoEntrega();
        e.setEmpresa(empresa);
        e.setApelido("Loja " + seq());
        e.setLogradouro("Rua Fictícia");
        e.setNumero("10");
        e.setBairro("Centro");
        e.setCidade("Caruaru");
        e.setUf("PE");
        e.setCep("55000-000");
        e.setPrincipal(true);
        return enderecoRepository.save(e);
    }

    protected CartaoPagamentoSalvo novoCartao(Empresa empresa) {
        CartaoPagamentoSalvo c = new CartaoPagamentoSalvo();
        c.setEmpresa(empresa);
        c.setTipo("credito");
        c.setBandeira("Visa");
        c.setUltimosDigitos("4242");
        c.setNumeroMascarado("•••• •••• •••• 4242");
        c.setValidade("12/30");
        c.setTitular("TITULAR TESTE");
        return cartaoRepository.save(c);
    }

    protected Barraca novaBarraca(Conta conta) {
        Evento ev = new Evento();
        ev.setEmpresa(conta.empresa());
        ev.setNome("Evento " + seq());
        ev.setDataInicio(LocalDate.now());
        ev.setDataFim(LocalDate.now().plusDays(10));
        ev.setStatus("ativo");
        ev = eventoRepository.save(ev);
        Barraca b = new Barraca();
        b.setEvento(ev);
        b.setNome("Barraca " + seq());
        b.setResponsavel(conta.usuario());
        b.setAtiva(1);
        return barracaRepository.save(b);
    }

    // ── HTTP ──────────────────────────────────────────────────────────────
    protected MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder req, Conta conta) {
        return req.header("Authorization", "Bearer " + conta.token());
    }

    protected MockHttpServletRequestBuilder comCorpo(MockHttpServletRequestBuilder req, Object corpo) throws Exception {
        return req.contentType(MediaType.APPLICATION_JSON)
                .content(corpo instanceof String s ? s : json.writeValueAsString(corpo));
    }

    protected MvcResult chamar(MockHttpServletRequestBuilder req) throws Exception {
        return mvc.perform(req).andReturn();
    }

    protected int status(MockHttpServletRequestBuilder req) throws Exception {
        return chamar(req).getResponse().getStatus();
    }

    protected JsonNode corpo(MvcResult r) throws Exception {
        return json.readTree(r.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
    }

    protected MockHttpServletRequestBuilder GET(String url) { return get(url); }
    protected MockHttpServletRequestBuilder POST(String url) { return post(url); }
    protected MockHttpServletRequestBuilder PUT(String url) { return put(url); }
    protected MockHttpServletRequestBuilder PATCH(String url) { return patch(url); }
    protected MockHttpServletRequestBuilder DELETE(String url) { return delete(url); }
}
