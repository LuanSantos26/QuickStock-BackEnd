package com.stock4event.backend.unit;

import com.stock4event.backend.entity.Usuario;
import com.stock4event.backend.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/** Validação do JWT: assinatura, expiração e formato. */
class JwtServiceTest {

    private static final String SEGREDO = "SegredoDeTesteUnitarioComMaisDe32Caracteres!!";
    private JwtService jwt;

    @BeforeEach
    void setUp() {
        jwt = new JwtService();
        ReflectionTestUtils.setField(jwt, "secret", SEGREDO);
        ReflectionTestUtils.setField(jwt, "expirationMs", 60_000L);
    }

    private Usuario usuario(long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail("teste@teste.local");
        return u;
    }

    @Test
    void tokenGeradoEhValidoELevaOIdDoUsuario() {
        String token = jwt.generateToken(usuario(42));
        assertThat(jwt.isTokenValid(token)).isTrue();
        assertThat(jwt.getUserId(token)).isEqualTo(42L);
    }

    @Test
    void tokenAssinadoComOutraChaveEhRecusado() {
        String falso = Jwts.builder()
                .subject("42")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor("OutraChaveQualquerComMaisDe32Caracteres!!".getBytes(StandardCharsets.UTF_8)))
                .compact();
        assertThat(jwt.isTokenValid(falso)).isFalse();
    }

    @Test
    void tokenExpiradoEhRecusado() {
        String expirado = Jwts.builder()
                .subject("42")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(SEGREDO.getBytes(StandardCharsets.UTF_8)))
                .compact();
        assertThat(jwt.isTokenValid(expirado)).isFalse();
    }

    @Test
    void tokenSemAssinaturaOuMalformadoEhRecusado() {
        String semAssinatura = Jwts.builder().subject("42").compact();
        assertThat(jwt.isTokenValid(semAssinatura)).isFalse();
        assertThat(jwt.isTokenValid("abc.def.ghi")).isFalse();
        assertThat(jwt.isTokenValid("")).isFalse();
    }

    @Test
    void tokenAdulteradoEhRecusado() {
        String token = jwt.generateToken(usuario(42));
        String[] partes = token.split("\\.");
        String payloadFalso = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"1\",\"exp\":9999999999}".getBytes(StandardCharsets.UTF_8));
        assertThat(jwt.isTokenValid(partes[0] + "." + payloadFalso + "." + partes[2])).isFalse();
    }
}
