package com.stock4event.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock4event.backend.repository.UsuarioRepository;
import com.stock4event.backend.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Regras de acesso da API.
 * Públicas: login, cadastro de conta, recuperação de senha, imagens (/uploads) e, se habilitado, o Swagger.
 * Todo o resto exige um JWT válido. Sem token válido: 401. Autenticado sem permissão: 403.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] SWAGGER = {"/swagger", "/swagger/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"};

    @Value("${stock4event.seguranca.swagger-publico:false}")
    private boolean swaggerPublico;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
                                                   UsuarioRepository usuarioRepository,
                                                   ObjectMapper objectMapper) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // API stateless com token no header, sem cookies
                .cors(Customizer.withDefaults())       // usa a configuração do WebConfig
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                responderErro(res, objectMapper, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Autenticação necessária. Faça login novamente."))
                        .accessDeniedHandler((req, res, ex) ->
                                responderErro(res, objectMapper, HttpServletResponse.SC_FORBIDDEN,
                                        "Você não tem permissão para acessar este recurso.")))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    auth.requestMatchers(HttpMethod.POST,
                            "/api/usuarios/login", "/api/cadastro", "/api/usuarios/recuperar-senha").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/uploads/**").permitAll();
                    auth.requestMatchers("/error").permitAll();
                    if (swaggerPublico) {
                        auth.requestMatchers(SWAGGER).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, usuarioRepository),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void responderErro(HttpServletResponse res, ObjectMapper mapper, int status, String mensagem)
            throws IOException {
        res.setStatus(status);
        res.setCharacterEncoding("UTF-8");
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(res.getOutputStream(), Map.of("erro", mensagem));
    }
}
