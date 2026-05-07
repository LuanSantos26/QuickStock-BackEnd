package com.quickstock.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "companies")
@Data // O Lombok cria os getters e setters automaticamente
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;      // Referente ao "Nome da Empresa" no front
    private String email;     // Referente ao "E-mail"
    private String password;  // Referente à "Senha"
    private String cnpj;      // Referente ao "CNPJ"
    private String phone;     // Referente ao "Telefone"
}