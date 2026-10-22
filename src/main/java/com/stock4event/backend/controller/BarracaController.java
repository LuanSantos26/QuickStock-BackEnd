package com.stock4event.backend.controller;

import com.stock4event.backend.dto.BarracaEstoqueRequestDTO;
import com.stock4event.backend.dto.BarracaRequestDTO;
import com.stock4event.backend.dto.BarracaResponseDTO;
import com.stock4event.backend.security.AcessoService;
import com.stock4event.backend.service.BarracaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/barracas")
@CrossOrigin(origins = "*")
public class BarracaController {

    @Autowired private BarracaService barracaService;
    @Autowired private AcessoService acesso;

    @GetMapping
    public List<BarracaResponseDTO> listar(@RequestParam(required = false) Long empresaId) {
        return barracaService.listarPorEmpresa(acesso.resolverEmpresa(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarracaResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(barracaService.buscarPorId(id, acesso.empresaId()));
    }

    @PostMapping
    public ResponseEntity<BarracaResponseDTO> criar(@Valid @RequestBody BarracaRequestDTO dto) {
        dto.setEmpresaId(acesso.resolverEmpresa(dto.getEmpresaId()));
        return ResponseEntity.status(201).body(barracaService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BarracaResponseDTO> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody BarracaRequestDTO dto) {
        dto.setEmpresaId(acesso.resolverEmpresa(dto.getEmpresaId()));
        return ResponseEntity.ok(barracaService.atualizar(id, dto));
    }

    @PutMapping("/{id}/estoque")
    public ResponseEntity<BarracaResponseDTO> atualizarEstoque(@PathVariable Long id,
                                                                @Valid @RequestBody BarracaEstoqueRequestDTO dto) {
        return ResponseEntity.ok(barracaService.sincronizarEstoque(id, acesso.empresaId(), dto.getItens()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id,
                                          @RequestParam(required = false) Long empresaId) {
        barracaService.desativar(id, acesso.resolverEmpresa(empresaId));
        return ResponseEntity.noContent().build();
    }
}
