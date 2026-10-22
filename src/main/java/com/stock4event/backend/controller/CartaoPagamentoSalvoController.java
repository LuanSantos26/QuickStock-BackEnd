package com.stock4event.backend.controller;

import com.stock4event.backend.dto.CartaoPagamentoSalvoDTO;
import com.stock4event.backend.dto.CartaoPagamentoSalvoRequestDTO;
import com.stock4event.backend.security.AcessoService;
import com.stock4event.backend.service.CartaoPagamentoSalvoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cartoes-pagamento")
@CrossOrigin(origins = "*")
public class CartaoPagamentoSalvoController {

    @Autowired private CartaoPagamentoSalvoService cartaoPagamentoSalvoService;
    @Autowired private AcessoService acesso;

    @GetMapping
    public List<CartaoPagamentoSalvoDTO> listar(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) String tipo) {
        return cartaoPagamentoSalvoService.listarPorEmpresa(acesso.resolverEmpresa(empresaId), tipo);
    }

    @PostMapping
    public ResponseEntity<CartaoPagamentoSalvoDTO> criar(
            @Valid @RequestBody CartaoPagamentoSalvoRequestDTO dto) {
        dto.setEmpresaId(acesso.resolverEmpresa(dto.getEmpresaId()));
        return ResponseEntity.status(201).body(cartaoPagamentoSalvoService.criar(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id, @RequestParam(required = false) Long empresaId) {
        cartaoPagamentoSalvoService.remover(id, acesso.resolverEmpresa(empresaId));
        return ResponseEntity.noContent().build();
    }
}
