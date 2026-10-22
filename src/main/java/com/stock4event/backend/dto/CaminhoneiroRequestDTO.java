package com.stock4event.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CaminhoneiroRequestDTO {

    /** Opcional: a empresa vem do token; se informada, precisa ser a do usuário logado. */
    private Long empresaId;

    @NotBlank(message = "Nome do caminhoneiro é obrigatório.")
    @Size(max = 120)
    private String nome;

    @NotBlank(message = "Veículo é obrigatório.")
    @Size(max = 80)
    private String veiculo;

    @NotBlank(message = "Rota é obrigatória.")
    @Size(max = 150)
    private String rota;

    @Valid
    private EntregaLogisticaRequestDTO entrega;
}
