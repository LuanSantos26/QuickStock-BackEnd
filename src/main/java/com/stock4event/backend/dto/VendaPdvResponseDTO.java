package com.stock4event.backend.dto;

import com.stock4event.backend.entity.ItemPedido;
import com.stock4event.backend.entity.Pedido;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Resumo da venda do PDV, usado na confirmação e no comprovante (sem valor fiscal). */
@Getter
public class VendaPdvResponseDTO {

    private final Long id;
    private final String numero;
    private final LocalDateTime criadoEm;
    private final String status;
    private final Long barracaId;
    private final String barracaNome;
    private final String operadorNome;
    private final String empresaNome;
    private final String empresaCnpj;
    private final String formaPagamento;
    private final BigDecimal total;
    private final BigDecimal valorRecebido;
    private final BigDecimal troco;
    private final String clienteNome;
    /** Documento do cliente mascarado (só os últimos dígitos aparecem). */
    private final String clienteDocumento;
    private final List<Item> itens;

    public VendaPdvResponseDTO(Pedido pedido, List<ItemPedido> itens) {
        this.id = pedido.getId();
        this.numero = String.format("PDV-%06d", pedido.getId());
        this.criadoEm = pedido.getCriadoEm();
        this.status = pedido.getStatus();
        this.barracaId = pedido.getBarraca().getId();
        this.barracaNome = pedido.getBarraca().getNome();
        this.operadorNome = pedido.getOperador().getNome();
        this.empresaNome = pedido.getBarraca().getEvento().getEmpresa().getNome();
        this.empresaCnpj = pedido.getBarraca().getEvento().getEmpresa().getCnpj();
        this.formaPagamento = pedido.getMetodoPagamento();
        this.total = pedido.getValorTotal();
        this.valorRecebido = pedido.getValorRecebido();
        this.troco = pedido.getTroco();
        this.clienteNome = pedido.getClienteNome();
        this.clienteDocumento = mascarar(pedido.getClienteDocumento());
        this.itens = itens.stream().map(Item::new).toList();
    }

    static String mascarar(String documento) {
        if (documento == null || documento.isBlank()) return null;
        String fim = documento.substring(Math.max(0, documento.length() - 2));
        return documento.length() == 11 ? "***.***.***-" + fim : "**.***.***/****-" + fim;
    }

    @Getter
    public static class Item {
        private final Long produtoId;
        private final String codigo;
        private final String nome;
        private final String unidade;
        private final BigDecimal quantidade;
        private final BigDecimal precoUnitario;
        private final BigDecimal subtotal;

        Item(ItemPedido item) {
            this.produtoId = item.getProduto().getId();
            this.codigo = item.getProduto().getCodigo();
            this.nome = item.getProduto().getNome();
            this.unidade = item.getProduto().getUnidade();
            this.quantidade = item.getQuantidade();
            this.precoUnitario = item.getPrecoUnitario();
            this.subtotal = item.getSubtotal();
        }
    }
}
