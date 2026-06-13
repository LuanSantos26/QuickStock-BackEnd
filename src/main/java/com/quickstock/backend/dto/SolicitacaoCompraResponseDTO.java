package com.quickstock.backend.dto;

import com.quickstock.backend.entity.ItemSolicitacaoCompra;
import com.quickstock.backend.entity.SolicitacaoCompra;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SolicitacaoCompraResponseDTO {

    private final Long id;
    private final Long fornecedorId;
    private final String fornecedorNome;
    private final String status;
    private final BigDecimal valorTotal;
    private final String observacao;
    private final String metodoPagamento;
    private final String enderecoResumo;
    private final BigDecimal taxaEntrega;
    private final LocalDateTime criadoEm;
    private final List<ItemSolicitacaoResponseDTO> itens;
    private final String statusLabel;
    private final List<StatusPedidoDTO> etapas;

    public SolicitacaoCompraResponseDTO(
            SolicitacaoCompra solicitacao,
            List<ItemSolicitacaoCompra> itens,
            String statusLabel,
            List<StatusPedidoDTO> etapas) {
        this.id = solicitacao.getId();
        this.fornecedorId = solicitacao.getEmpresaFornecedora().getId();
        this.fornecedorNome = solicitacao.getEmpresaFornecedora().getNome();
        this.status = solicitacao.getStatus();
        this.valorTotal = solicitacao.getValorTotal();
        this.observacao = solicitacao.getObservacao();
        this.metodoPagamento = solicitacao.getMetodoPagamento();
        this.enderecoResumo = solicitacao.getEnderecoResumo();
        this.taxaEntrega = solicitacao.getTaxaEntrega();
        this.criadoEm = solicitacao.getCriadoEm();
        this.itens = itens.stream().map(ItemSolicitacaoResponseDTO::new).toList();
        this.statusLabel = statusLabel;
        this.etapas = etapas;
    }

    public SolicitacaoCompraResponseDTO(SolicitacaoCompra solicitacao, List<ItemSolicitacaoCompra> itens) {
        this(solicitacao, itens, null, List.of());
    }
}
