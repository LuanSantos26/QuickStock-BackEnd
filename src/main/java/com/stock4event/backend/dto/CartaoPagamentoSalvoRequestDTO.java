package com.stock4event.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartaoPagamentoSalvoRequestDTO {

    /** Opcional: a empresa vem do token; se informada, precisa ser a do usuário logado. */
    private Long empresaId;

    @NotBlank
    @Size(max = 20)
    private String tipo;

    @Size(max = 80)
    private String apelido;

    @NotBlank
    @Size(max = 40)
    private String bandeira;

    @NotBlank
    @Size(max = 4)
    private String ultimosDigitos;

    @NotBlank
    @Size(max = 5)
    private String validade;

    @NotBlank
    @Size(max = 120)
    private String titular;
}
