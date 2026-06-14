package com.quickstock.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desativa a proteção CSRF, pois APIs REST com JWT não precisam disso
                .csrf(csrf -> csrf.disable())

                // Define que a API não guardará sessão de usuário (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Regras de acesso às rotas
                .authorizeHttpRequests(authorize -> authorize
                        // LIBERA GERAL AS ROTAS DO SWAGGER PARA VOCÊ CONSEGUIR VER A DOCUMENTAÇÃO
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Você pode liberar rotas de login/cadastro aqui no futuro, por exemplo:
                        // .requestMatchers("/api/usuarios/login").permitAll()

                        // Exige autenticação para qualquer outra rota que não foi citada acima
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}