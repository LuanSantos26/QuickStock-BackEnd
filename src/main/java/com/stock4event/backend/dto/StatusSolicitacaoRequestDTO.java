package com.stock4event.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusSolicitacaoRequestDTO {

    /** Opcional: a empresa fornecedora vem do token; se informada, precisa ser a do usuário logado. */
    private Long empresaFornecedoraId;

    /** em_rota, entregue ou cancelada. */
    @NotBlank
    private String status;
}
