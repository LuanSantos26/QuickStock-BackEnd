package com.stock4event.backend.controller;

import com.stock4event.backend.dto.FormaPagamentoSalvaDTO;
import com.stock4event.backend.dto.FormaPagamentoSalvaRequestDTO;
import com.stock4event.backend.security.AcessoService;
import com.stock4event.backend.service.FormaPagamentoSalvaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formas-pagamento")
@CrossOrigin(origins = "*")
public class FormaPagamentoSalvaController {

    @Autowired private FormaPagamentoSalvaService formaPagamentoSalvaService;
    @Autowired private AcessoService acesso;

    @GetMapping
    public List<FormaPagamentoSalvaDTO> listar(@RequestParam(required = false) Long empresaId) {
        return formaPagamentoSalvaService.listarPorEmpresa(acesso.resolverEmpresa(empresaId));
    }

    @PostMapping
    public ResponseEntity<FormaPagamentoSalvaDTO> criar(@Valid @RequestBody FormaPagamentoSalvaRequestDTO dto) {
        dto.setEmpresaId(acesso.resolverEmpresa(dto.getEmpresaId()));
        return ResponseEntity.status(201).body(formaPagamentoSalvaService.criar(dto));
    }

    @PatchMapping("/{id}/principal")
    public FormaPagamentoSalvaDTO definirPrincipal(@PathVariable Long id, @RequestParam(required = false) Long empresaId) {
        return formaPagamentoSalvaService.definirPrincipal(id, acesso.resolverEmpresa(empresaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id, @RequestParam(required = false) Long empresaId) {
        formaPagamentoSalvaService.remover(id, acesso.resolverEmpresa(empresaId));
        return ResponseEntity.noContent().build();
    }
}
