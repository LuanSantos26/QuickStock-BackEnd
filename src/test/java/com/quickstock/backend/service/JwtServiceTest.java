package com.quickstock.backend.service;

import com.quickstock.backend.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secret", "QuickStockTestSecretKeyWithAtLeast32Bytes");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 60_000L);
    }

    @Test
    void generatesAndReadsAValidToken() {
        Usuario usuario = new Usuario();
        usuario.setId(42L);
        usuario.setEmail("teste@quickstock.com");

        String token = jwtService.generateToken(usuario);

        assertTrue(jwtService.isTokenValid(token));
        assertEquals(42L, jwtService.getUserId(token));
    }

    @Test
    void rejectsAnInvalidToken() {
        assertFalse(jwtService.isTokenValid("token-invalido"));
    }
}
