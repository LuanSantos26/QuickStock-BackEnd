package com.stock4event.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BarracaEstoqueRequestDTO {

    @Valid
    private List<@NotNull(message = "Item inválido.") EstoqueItemRequestDTO> itens = new ArrayList<>();
}
