package com.stock4event.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmpresaRequestDTO {

    @NotBlank(message = "Nome da empresa é obrigatório.")
    @Size(max = 150)
    private String nome;

    @NotBlank(message = "CNPJ é obrigatório.")
    @Size(max = 18)
    private String cnpj;

    @Size(max = 20)
    private String telefone;

    /** Opcionais (perfil da loja): null mantém o valor atual, texto vazio remove. */
    @Size(max = 300, message = "Descrição deve ter no máximo 300 caracteres.")
    private String descricao;

    @Size(max = 500)
    private String logoUrl;

    @Size(max = 500)
    private String capaUrl;
}
