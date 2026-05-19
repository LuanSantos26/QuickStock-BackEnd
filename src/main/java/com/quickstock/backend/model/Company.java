package com.quickstock.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "empresas")
@Data // O Lombok cria os getters e setters automaticamente
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 18)
    private String cnpj;

    @Column(length = 20)
    private String telefone;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}