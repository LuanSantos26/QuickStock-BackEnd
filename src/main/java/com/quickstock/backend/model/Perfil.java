package com.quickstock.backend.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "perfis")
@Data


public class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(length = 200)
    private String descricao;
}
