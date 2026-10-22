package com.stock4event.backend.dto;

import com.stock4event.backend.entity.Barraca;
import com.stock4event.backend.entity.Empresa;
import com.stock4event.backend.entity.EstoqueBarraca;
import com.stock4event.backend.entity.Evento;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Quiosque de outra empresa, visível na vitrine "Quiosques disponíveis".
 * Mostra o que está à venda e se há estoque, sem expor quantidades internas.
 */
@Getter
public class QuiosqueDisponivelDTO {

    private final Long id;
    private final String nome;
    private final String eventoNome;
    private final LocalDate dataInicio;
    private final LocalDate dataFim;
    private final Long empresaId;
    private final String empresaNome;
    private final String empresaTipo;
    private final String empresaDescricao;
    private final String empresaLogoUrl;
    private final String empresaCapaUrl;
    /** Verdadeiro quando a dona do quiosque também vende no marketplace (dá para abrir a loja dela). */
    private final boolean distribuidora;
    private final int totalProdutos;
    private final int produtosDisponiveis;
    private final List<Item> produtos;

    public QuiosqueDisponivelDTO(Barraca barraca, List<EstoqueBarraca> estoque, boolean distribuidora) {
        Evento evento = barraca.getEvento();
        Empresa empresa = evento.getEmpresa();
        this.id = barraca.getId();
        this.nome = barraca.getNome();
        this.eventoNome = evento.getNome();
        this.dataInicio = evento.getDataInicio();
        this.dataFim = evento.getDataFim();
        this.empresaId = empresa.getId();
        this.empresaNome = empresa.getNome();
        this.empresaTipo = empresa.getTipo();
        this.empresaDescricao = empresa.getDescricao();
        this.empresaLogoUrl = empresa.getLogoUrl();
        this.empresaCapaUrl = empresa.getCapaUrl();
        this.distribuidora = distribuidora;
        this.produtos = estoque.stream().map(Item::new).toList();
        this.totalProdutos = produtos.size();
        this.produtosDisponiveis = (int) produtos.stream().filter(Item::isDisponivel).count();
    }

    @Getter
    public static class Item {
        private final Long produtoId;
        private final String nome;
        private final String unidade;
        private final BigDecimal precoVenda;
        private final String imagemUrl;
        private final boolean disponivel;

        Item(EstoqueBarraca e) {
            this.produtoId = e.getProduto().getId();
            this.nome = e.getProduto().getNome();
            this.unidade = e.getProduto().getUnidade();
            this.precoVenda = e.getProduto().getPrecoVenda();
            this.imagemUrl = e.getProduto().getImagemUrl();
            this.disponivel = ProdutoPdvDTO.disponivel(e, e.getProduto()).signum() > 0;
        }
    }
}
