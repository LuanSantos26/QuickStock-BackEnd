package com.quickstock.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProdutoRequestDTO {

    @NotBlank
    @Size(max = 150)
    private String nome;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precoVenda;

    @NotBlank
    @Size(max = 20)
    private String unidade;

    @Size(max = 500)
    private String descricao;

    @Size(max = 500)
    private String imagemUrl;

    @NotNull
    private Long empresaId;
}
