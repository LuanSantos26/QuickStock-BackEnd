package com.stock4event.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EnderecoEntregaRequestDTO {

    /** Opcional: a empresa vem do token; se informada, precisa ser a do usuário logado. */
    private Long empresaId;

    @NotBlank
    @Size(max = 80)
    private String apelido;

    @NotBlank
    @Size(max = 150)
    private String logradouro;

    @NotBlank
    @Size(max = 20)
    private String numero;

    @Size(max = 80)
    private String complemento;

    @NotBlank
    @Size(max = 80)
    private String bairro;

    @NotBlank
    @Size(max = 80)
    private String cidade;

    @NotBlank
    @Size(max = 2)
    private String uf;

    @NotBlank
    @Size(max = 9)
    private String cep;

    private boolean principal;
}
