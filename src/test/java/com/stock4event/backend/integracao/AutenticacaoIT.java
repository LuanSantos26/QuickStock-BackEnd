package com.stock4event.backend.integracao;

import com.stock4event.backend.entity.Usuario;
import com.stock4event.backend.support.IntegracaoBase;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** 401 sem autenticação válida; rotas públicas continuam abertas. */
class AutenticacaoIT extends IntegracaoBase {

    @Value("${jwt.secret}")
    private String segredo;

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/usuarios/me", "/api/usuarios", "/api/empresas", "/api/perfis", "/api/produtos",
            "/api/enderecos", "/api/marketplace/fornecedores", "/api/solicitacoes-compra",
            "/api/solicitacoes-compra/recebidas", "/api/barracas", "/api/caminhoneiros", "/api/logistica",
            "/api/cartoes-pagamento", "/api/chaves-pix", "/api/formas-pagamento", "/api/financeiro/resumo",
            "/api/notificacoes", "/api/pedidos", "/api/itens-pedido", "/api/pagamentos", "/api/eventos",
            "/api/estoque-barraca", "/api/enderecos/cep/55000000", "/v3/api-docs", "/swagger-ui/index.html"
    })
    void rotasProtegidasSemTokenRespondem401(String url) throws Exception {
        MvcResult r = chamar(GET(url));
        assertThat(r.getResponse().getStatus()).as(url).isEqualTo(401);
        assertThat(corpo(r).path("erro").asText()).isNotBlank();
    }

    @Test
    void escritaSemTokenRespondem401ENaoGrava() throws Exception {
        long antes = produtoRepository.count();
        int s = status(comCorpo(POST("/api/produtos"),
                Map.of("nome", "Sem token", "precoVenda", 1, "unidade", "UN", "empresaId", 1)));
        assertThat(s).isEqualTo(401);
        assertThat(produtoRepository.count()).isEqualTo(antes);
        assertThat(status(DELETE("/api/produtos/1"))).isEqualTo(401);
    }

    @Test
    void tokenInvalidoOuMalformadoResponde401() throws Exception {
        assertThat(status(GET("/api/usuarios/me").header("Authorization", "Bearer abc.def.ghi"))).isEqualTo(401);
        assertThat(status(GET("/api/usuarios/me").header("Authorization", "Bearer "))).isEqualTo(401);
        assertThat(status(GET("/api/usuarios/me").header("Authorization", "Token qualquer"))).isEqualTo(401);
    }

    @Test
    void tokenAssinadoComOutraChaveResponde401() throws Exception {
        Conta c = novoCliente();
        String falso = Jwts.builder().subject(String.valueOf(c.usuarioId()))
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor("ChaveDeUmAtacanteComMaisDe32Caracteres!!!!".getBytes(StandardCharsets.UTF_8)))
                .compact();
        assertThat(status(GET("/api/usuarios/me").header("Authorization", "Bearer " + falso))).isEqualTo(401);
    }

    @Test
    void tokenExpiradoResponde401() throws Exception {
        Conta c = novoCliente();
        String expirado = Jwts.builder().subject(String.valueOf(c.usuarioId()))
                .issuedAt(new Date(System.currentTimeMillis() - 7_200_000))
                .expiration(new Date(System.currentTimeMillis() - 3_600_000))
                .signWith(Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8)))
                .compact();
        assertThat(status(GET("/api/enderecos").header("Authorization", "Bearer " + expirado))).isEqualTo(401);
    }

    @Test
    void tokenDeUsuarioDesativadoOuInexistenteResponde401() throws Exception {
        Conta c = novoCliente();
        Usuario u = usuarioRepository.findById(c.usuarioId()).orElseThrow();
        u.setAtivo(0);
        usuarioRepository.save(u);
        assertThat(status(auth(GET("/api/usuarios/me"), c))).isEqualTo(401);

        String fantasma = Jwts.builder().subject("987654321")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8)))
                .compact();
        assertThat(status(GET("/api/usuarios/me").header("Authorization", "Bearer " + fantasma))).isEqualTo(401);
    }

    @Test
    void tokenValidoAcessaRotaProtegida() throws Exception {
        Conta c = novoCliente();
        MvcResult r = chamar(auth(GET("/api/usuarios/me"), c));
        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        assertThat(corpo(r).path("id").asLong()).isEqualTo(c.usuarioId());
        assertThat(corpo(r).path("empresa").path("id").asLong()).isEqualTo(c.empresaId());
    }

    @Test
    void loginEhPublicoEDevolveTokenQueFunciona() throws Exception {
        Conta c = novoCliente();
        MvcResult r = chamar(comCorpo(POST("/api/usuarios/login"),
                Map.of("email", c.usuario().getEmail(), "senha", SENHA)));
        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        String token = corpo(r).path("token").asText();
        assertThat(status(GET("/api/usuarios/me").header("Authorization", "Bearer " + token))).isEqualTo(200);

        assertThat(status(comCorpo(POST("/api/usuarios/login"),
                Map.of("email", c.usuario().getEmail(), "senha", "errada")))).isEqualTo(401);
    }

    @Test
    void loginComTokenAntigoNoHeaderContinuaFuncionando() throws Exception {
        Conta c = novoCliente();
        int s = status(comCorpo(POST("/api/usuarios/login"), Map.of("email", c.usuario().getEmail(), "senha", SENHA))
                .header("Authorization", "Bearer token.antigo.invalido"));
        assertThat(s).isEqualTo(200);
    }

    @Test
    void cadastroERecuperacaoDeSenhaSaoPublicos() throws Exception {
        int n = seq();
        int s = status(comCorpo(POST("/api/cadastro"), Map.of(
                "perfil", "Cliente",
                "empresa", Map.of("nome", "Bar Público " + n, "cnpj", String.format("77.%03d.000/0001-00", n)),
                "usuario", Map.of("nome", "Dono " + n, "email", "publico" + n + "@teste.local", "senha", "senha123"))));
        assertThat(s).isEqualTo(201);

        assertThat(status(comCorpo(POST("/api/usuarios/recuperar-senha"),
                Map.of("email", "publico" + n + "@teste.local", "novaSenha", "novaSenha1")))).isEqualTo(200);
        assertThat(status(comCorpo(POST("/api/usuarios/login"),
                Map.of("email", "publico" + n + "@teste.local", "senha", "novaSenha1")))).isEqualTo(200);
    }

    @Test
    void imagensSaoPublicas() throws Exception {
        assertThat(status(GET("/uploads/empresas/casa-dos-vinhos-logo.png"))).isEqualTo(200);
    }
}
