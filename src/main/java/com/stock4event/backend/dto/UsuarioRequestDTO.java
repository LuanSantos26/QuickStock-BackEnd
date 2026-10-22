package com.stock4event.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioRequestDTO {

    public static final String EMAIL_FORMATO = "^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$";

    @NotBlank(message = "Nome é obrigatório.")
    @Size(max = 100)
    private String nome;

    @NotBlank(message = "E-mail é obrigatório.")
    // Além da regra padrão do @Email, exige domínio com ponto (ex.: "ana@exemplo.com"; "ana@exemplo" não passa).
    // Formato válido não prova que o endereço existe nem que pertence a quem se cadastrou.
    @Email(message = "E-mail inválido.", regexp = EMAIL_FORMATO)
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Senha é obrigatória.")
    @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres.")
    private String senha;

    @Size(max = 14)
    private String cpf;
}
