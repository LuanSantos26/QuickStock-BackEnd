package com.stock4event.backend.security;

/** Dados do usuário dono do token, colocados no SecurityContext pelo JwtAuthenticationFilter. */
public record UsuarioAutenticado(Long id, Long empresaId, String email) {
}
