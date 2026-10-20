package com.quickstock.backend.controller;

import com.quickstock.backend.dto.SolicitacaoCompraRequestDTO;
import com.quickstock.backend.dto.SolicitacaoCompraResponseDTO;
import com.quickstock.backend.service.SolicitacaoCompraService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitacoes-compra")
@CrossOrigin(origins = "*")
public class SolicitacaoCompraController {

    @Autowired private SolicitacaoCompraService solicitacaoCompraService;

    @GetMapping
    public List<SolicitacaoCompraResponseDTO> listar(
            @RequestParam Long empresaCompradoraId) {
        return solicitacaoCompraService.listarPorComprador(empresaCompradoraId);
    }

    @PostMapping
    public ResponseEntity<SolicitacaoCompraResponseDTO> criar(
            @Valid @RequestBody SolicitacaoCompraRequestDTO dto) {
        return ResponseEntity.status(201).body(solicitacaoCompraService.criar(dto));
    }

    @GetMapping("/{id}")
    public SolicitacaoCompraResponseDTO obter(
            @PathVariable Long id,
            @RequestParam Long empresaCompradoraId) {
        return solicitacaoCompraService.obterPorId(id, empresaCompradoraId);
    }
}
