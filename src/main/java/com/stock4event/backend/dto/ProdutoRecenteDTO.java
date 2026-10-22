package com.stock4event.backend.dto;

import com.stock4event.backend.entity.Empresa;
import com.stock4event.backend.entity.Produto;
import lombok.Getter;

import java.time.LocalDateTime;

/** Produto do marketplace com a distribuidora que o cadastrou (seção "Adicionados pelos fornecedores"). */
@Getter
public class ProdutoRecenteDTO extends ProdutoResponseDTO {

    private final String fornecedorNome;
    private final String fornecedorLogoUrl;
    private final String fornecedorCapaUrl;
    private final String fornecedorDescricao;
    private final String fornecedorTipo;
    private final LocalDateTime criadoEm;
    /** Produto da empresa do usuário logado (aparece, mas não pode ser comprado por ela mesma). */
    private final boolean proprio;

    public ProdutoRecenteDTO(Produto produto, Long empresaDoUsuario) {
        super(produto);
        Empresa e = produto.getEmpresa();
        this.fornecedorNome = e.getNome();
        this.fornecedorLogoUrl = e.getLogoUrl();
        this.fornecedorCapaUrl = e.getCapaUrl();
        this.fornecedorDescricao = e.getDescricao();
        this.fornecedorTipo = e.getTipo();
        this.criadoEm = produto.getCriadoEm();
        this.proprio = e.getId().equals(empresaDoUsuario);
    }
}
