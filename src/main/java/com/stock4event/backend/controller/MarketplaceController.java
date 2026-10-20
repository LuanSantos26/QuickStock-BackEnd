package com.stock4event.backend.controller;

import com.stock4event.backend.dto.ProdutoResponseDTO;
import com.stock4event.backend.dto.FornecedorResponseDTO;
import com.stock4event.backend.service.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
@CrossOrigin(origins = "*")
public class MarketplaceController {

    @Autowired private MarketplaceService marketplaceService;

    @GetMapping("/fornecedores")
    public List<FornecedorResponseDTO> listarFornecedores(
            @RequestParam(required = false) Long empresaCompradoraId) {
        return marketplaceService.listarFornecedores(empresaCompradoraId);
    }

    @GetMapping("/fornecedores/{id}/produtos")
    public List<ProdutoResponseDTO> listarProdutosFornecedor(@PathVariable Long id) {
        return marketplaceService.listarProdutosFornecedor(id);
    }
}
