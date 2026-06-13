package com.quickstock.backend.dto;

import com.quickstock.backend.entity.Produto;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProdutoResponseDTO {

    private final Long id;
    private final Long empresaId;
    private final String nome;
    private final BigDecimal precoVenda;
    private final String unidade;
    private final String descricao;
    private final String imagemUrl;
    private final Integer ativo;

    public ProdutoResponseDTO(Produto produto) {
        this.id = produto.getId();
        this.empresaId = produto.getEmpresa().getId();
        this.nome = produto.getNome();
        this.precoVenda = produto.getPrecoVenda();
        this.unidade = produto.getUnidade();
        this.descricao = produto.getDescricao();
        this.imagemUrl = produto.getImagemUrl();
        this.ativo = produto.getAtivo();
    }
}
