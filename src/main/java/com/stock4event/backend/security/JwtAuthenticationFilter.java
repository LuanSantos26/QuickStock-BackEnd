package com.stock4event.backend.security;

import com.stock4event.backend.repository.UsuarioRepository;
import com.stock4event.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Lê "Authorization: Bearer <token>", confere assinatura e expiração (JwtService) e se o usuário
 * ainda existe e está ativo. Se tudo bater, autentica a requisição; senão segue sem autenticação
 * e as rotas protegidas respondem 401. Não é um @Component para não ser registrado duas vezes.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = extrairToken(request.getHeader("Authorization"));
        if (token != null && jwtService.isTokenValid(token)) {
            Long usuarioId = idDoToken(token);
            if (usuarioId != null) {
                usuarioRepository.findById(usuarioId)
                        .filter(u -> Integer.valueOf(1).equals(u.getAtivo()))
                        .ifPresent(u -> {
                            var principal = new UsuarioAutenticado(u.getId(), u.getEmpresa().getId(), u.getEmail());
                            var papel = "ROLE_" + u.getPerfil().getNome().toUpperCase(Locale.ROOT);
                            var auth = new UsernamePasswordAuthenticationToken(
                                    principal, null, List.of(new SimpleGrantedAuthority(papel)));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
            }
        }
        chain.doFilter(request, response);
    }

    private Long idDoToken(String token) {
        try {
            return jwtService.getUserId(token);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String extrairToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
