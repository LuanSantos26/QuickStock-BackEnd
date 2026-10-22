package com.stock4event.backend.dto;

import com.stock4event.backend.entity.EstoqueBarraca;
import com.stock4event.backend.entity.Produto;
import lombok.Getter;

import java.math.BigDecimal;

/** Produto vendável num ponto de venda, com o preço cadastrado e a quantidade disponível ali. */
@Getter
public class ProdutoPdvDTO {

    private final Long id;
    private final String codigo;
    private final String nome;
    private final String unidade;
    private final BigDecimal precoVenda;
    private final String imagemUrl;
    /** Menor valor entre o estoque alocado no quiosque e o estoque total do produto. */
    private final BigDecimal disponivel;

    public ProdutoPdvDTO(EstoqueBarraca estoque) {
        Produto p = estoque.getProduto();
        this.id = p.getId();
        this.codigo = p.getCodigo();
        this.nome = p.getNome();
        this.unidade = p.getUnidade();
        this.precoVenda = p.getPrecoVenda();
        this.imagemUrl = p.getImagemUrl();
        this.disponivel = disponivel(estoque, p);
    }

    public static BigDecimal disponivel(EstoqueBarraca estoque, Produto produto) {
        BigDecimal noQuiosque = estoque.getQuantidade() != null ? estoque.getQuantidade() : BigDecimal.ZERO;
        BigDecimal total = produto.getEstoque() != null ? produto.getEstoque() : BigDecimal.ZERO;
        return noQuiosque.min(total).max(BigDecimal.ZERO);
    }
}
