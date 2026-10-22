package com.stock4event.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/** Item da venda: só o produto e a quantidade. O preço vem do cadastro. */
@Data
public class ItemVendaPdvRequestDTO {

    @NotNull(message = "Produto do item é obrigatório.")
    private Long produtoId;

    /** Mesmas regras de quantidade dos itens de pedido (mínimo 0,001; 3 casas decimais). */
    @NotNull(message = "Quantidade do item é obrigatória.")
    @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero.")
    @DecimalMax(value = "9999999", message = "Quantidade muito alta.")
    @Digits(integer = 7, fraction = 3, message = "Quantidade inválida.")
    private BigDecimal quantidade;
}
