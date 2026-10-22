package com.stock4event.backend.controller;

import com.stock4event.backend.dto.ProdutoResponseDTO;
import com.stock4event.backend.dto.FornecedorResponseDTO;
import com.stock4event.backend.dto.ProdutoRecenteDTO;
import com.stock4event.backend.dto.QuiosqueDisponivelDTO;
import com.stock4event.backend.exception.CadastroException;
import org.springframework.http.HttpStatus;
import com.stock4event.backend.security.AcessoService;
import com.stock4event.backend.service.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Vitrine B2B: qualquer usuário logado vê as distribuidoras e o catálogo ativo delas. */
@RestController
@RequestMapping("/api/marketplace")
@CrossOrigin(origins = "*")
public class MarketplaceController {

    @Autowired private MarketplaceService marketplaceService;
    @Autowired private AcessoService acesso;

    @GetMapping("/fornecedores")
    public List<FornecedorResponseDTO> listarFornecedores(
            @RequestParam(required = false) Long empresaCompradoraId) {
        return marketplaceService.listarFornecedores(acesso.resolverEmpresa(empresaCompradoraId));
    }

    @GetMapping("/fornecedores/{id}/produtos")
    public List<ProdutoResponseDTO> listarProdutosFornecedor(@PathVariable Long id) {
        return marketplaceService.listarProdutosFornecedor(id);
    }

    /** Produtos cadastrados por todas as contas fornecedoras, mais recentes primeiro. */
    @GetMapping("/produtos-recentes")
    public List<ProdutoRecenteDTO> listarProdutosRecentes(@RequestParam(defaultValue = "20") int limite) {
        return marketplaceService.listarProdutosRecentes(acesso.empresaId(), limite);
    }

    /** Quiosques ativos das outras empresas, com o que está à venda (sem quantidades). */
    @GetMapping("/quiosques")
    public List<QuiosqueDisponivelDTO> listarQuiosques(@RequestParam(required = false) String busca) {
        if (busca != null && busca.length() > 100) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Busca deve ter no máximo 100 caracteres.");
        }
        return marketplaceService.listarQuiosquesDisponiveis(acesso.empresaId(), busca);
    }
}
