package com.stock4event.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BarracaRequestDTO {

    @NotBlank
    @Size(max = 100)
    private String nome;

    /** Opcional: a empresa vem do token; se informada, precisa ser a do usuário logado. */
    private Long empresaId;

    @NotNull
    private Long responsavelId;

    @Valid
    private List<@NotNull(message = "Item inválido.") EstoqueItemRequestDTO> itens = new ArrayList<>();
}
