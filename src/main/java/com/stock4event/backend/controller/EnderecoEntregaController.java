package com.stock4event.backend.controller;

import com.stock4event.backend.dto.EnderecoEntregaDTO;
import com.stock4event.backend.dto.EnderecoEntregaRequestDTO;
import com.stock4event.backend.security.AcessoService;
import com.stock4event.backend.service.CepConsultaService;
import com.stock4event.backend.service.EnderecoEntregaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enderecos")
@CrossOrigin(origins = "*")
public class EnderecoEntregaController {

    @Autowired private EnderecoEntregaService enderecoEntregaService;
    @Autowired private CepConsultaService cepConsultaService;
    @Autowired private AcessoService acesso;

    @GetMapping
    public List<EnderecoEntregaDTO> listar(@RequestParam(required = false) Long empresaId) {
        return enderecoEntregaService.listarPorEmpresa(acesso.resolverEmpresa(empresaId));
    }

    @GetMapping("/cep/{cep}")
    public ResponseEntity<?> buscarCep(@PathVariable String cep) {
        return cepConsultaService.buscarPorCep(cep)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(Map.of("erro", "CEP não encontrado.")));
    }

    @PostMapping
    public ResponseEntity<EnderecoEntregaDTO> criar(@Valid @RequestBody EnderecoEntregaRequestDTO dto) {
        dto.setEmpresaId(acesso.resolverEmpresa(dto.getEmpresaId()));
        return ResponseEntity.status(201).body(enderecoEntregaService.criar(dto));
    }

    @PutMapping("/{id}")
    public EnderecoEntregaDTO atualizar(@PathVariable Long id, @Valid @RequestBody EnderecoEntregaRequestDTO dto) {
        dto.setEmpresaId(acesso.resolverEmpresa(dto.getEmpresaId()));
        return enderecoEntregaService.atualizar(id, dto);
    }

    @PatchMapping("/{id}/principal")
    public EnderecoEntregaDTO definirPrincipal(@PathVariable Long id, @RequestParam(required = false) Long empresaId) {
        return enderecoEntregaService.definirPrincipal(id, acesso.resolverEmpresa(empresaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id, @RequestParam(required = false) Long empresaId) {
        enderecoEntregaService.remover(id, acesso.resolverEmpresa(empresaId));
        return ResponseEntity.noContent().build();
    }
}
