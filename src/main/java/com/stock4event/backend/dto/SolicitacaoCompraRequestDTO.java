package com.stock4event.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class SolicitacaoCompraRequestDTO {

    /** Opcional: a empresa compradora vem do token; se informada, precisa ser a do usuário logado. */
    private Long empresaCompradoraId;

    @NotNull
    private Long empresaFornecedoraId;

    /** Opcional: o solicitante é sempre o usuário do token. */
    private Long usuarioId;

    @Size(max = 500)
    private String observacao;

    @NotBlank
    @Size(max = 20)
    private String metodoPagamento;

    private Long enderecoEntregaId;

    @DecimalMin(value = "0.0", message = "Taxa de entrega não pode ser negativa.")
    private BigDecimal taxaEntrega;

    @Size(max = 120)
    private String pagamentoReferencia;

    @Size(max = 300)
    private String pagamentoDetalhes;

    @Valid
    private List<@NotNull(message = "Item inválido.") ItemSolicitacaoRequestDTO> itens = new ArrayList<>();
}
