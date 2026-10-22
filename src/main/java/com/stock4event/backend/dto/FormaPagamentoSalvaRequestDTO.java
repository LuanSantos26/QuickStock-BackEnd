package com.stock4event.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FormaPagamentoSalvaRequestDTO {

    /** Opcional: a empresa vem do token; se informada, precisa ser a do usuário logado. */
    private Long empresaId;

    @NotBlank
    @Size(max = 20)
    private String tipo;

    @NotBlank
    @Size(max = 80)
    private String apelido;

    private boolean principal = false;
}
