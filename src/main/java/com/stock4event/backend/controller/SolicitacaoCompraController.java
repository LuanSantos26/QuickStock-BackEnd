package com.stock4event.backend.controller;

import com.stock4event.backend.dto.SolicitacaoCompraRequestDTO;
import com.stock4event.backend.dto.SolicitacaoCompraResponseDTO;
import com.stock4event.backend.dto.StatusSolicitacaoRequestDTO;
import com.stock4event.backend.security.AcessoService;
import com.stock4event.backend.service.SolicitacaoCompraService;
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
    @Autowired private AcessoService acesso;

    @GetMapping
    public List<SolicitacaoCompraResponseDTO> listar(
            @RequestParam(required = false) Long empresaCompradoraId) {
        return solicitacaoCompraService.listarPorComprador(acesso.resolverEmpresa(empresaCompradoraId));
    }

    /** A empresa compradora e o usuário solicitante são sempre os do token. */
    @PostMapping
    public ResponseEntity<SolicitacaoCompraResponseDTO> criar(
            @Valid @RequestBody SolicitacaoCompraRequestDTO dto) {
        dto.setEmpresaCompradoraId(acesso.resolverEmpresa(dto.getEmpresaCompradoraId()));
        Long usuarioId = acesso.usuarioId();
        if (dto.getUsuarioId() != null && !dto.getUsuarioId().equals(usuarioId)) {
            throw AcessoService.proibido("O pedido só pode ser feito em nome do usuário logado.");
        }
        dto.setUsuarioId(usuarioId);
        return ResponseEntity.status(201).body(solicitacaoCompraService.criar(dto));
    }

    @GetMapping("/{id}")
    public SolicitacaoCompraResponseDTO obter(
            @PathVariable Long id,
            @RequestParam(required = false) Long empresaCompradoraId) {
        return solicitacaoCompraService.obterPorId(id, acesso.resolverEmpresa(empresaCompradoraId));
    }

    /** Pedidos recebidos pelo fornecedor. */
    @GetMapping("/recebidas")
    public List<SolicitacaoCompraResponseDTO> listarRecebidas(@RequestParam(required = false) Long empresaFornecedoraId) {
        return solicitacaoCompraService.listarRecebidas(acesso.resolverEmpresa(empresaFornecedoraId));
    }

    @PatchMapping("/{id}/status")
    public SolicitacaoCompraResponseDTO atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusSolicitacaoRequestDTO dto) {
        return solicitacaoCompraService.atualizarStatus(id, acesso.resolverEmpresa(dto.getEmpresaFornecedoraId()), dto.getStatus());
    }

    @PatchMapping("/{id}/cancelar")
    public SolicitacaoCompraResponseDTO cancelar(
            @PathVariable Long id,
            @RequestParam(required = false) Long empresaCompradoraId) {
        return solicitacaoCompraService.cancelarPeloComprador(id, acesso.resolverEmpresa(empresaCompradoraId));
    }
}
