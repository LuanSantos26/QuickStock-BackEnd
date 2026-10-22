package com.stock4event.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Campos que o próprio usuário pode alterar em PUT /api/usuarios/{id}.
 * Empresa, perfil de acesso e status (ativo) são controlados pelo servidor e não são aceitos aqui.
 * Campos nulos não são alterados.
 */
@Data
public class UsuarioAtualizacaoDTO {

    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres.")
    @Pattern(regexp = ".*\\S.*", message = "Nome não pode ficar em branco.")
    private String nome;

    @Email(message = "E-mail inválido.")
    @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres.")
    @Pattern(regexp = ".*\\S.*", message = "E-mail não pode ficar em branco.")
    private String email;

    @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres.")
    private String senha;
}
