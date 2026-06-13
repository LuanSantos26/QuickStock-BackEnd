package com.quickstock.backend.controller;

import com.quickstock.backend.dto.EnderecoEntregaDTO;
import com.quickstock.backend.dto.EnderecoEntregaRequestDTO;
import com.quickstock.backend.service.EnderecoEntregaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enderecos")
@CrossOrigin(origins = "*")
public class EnderecoEntregaController {

    @Autowired private EnderecoEntregaService enderecoEntregaService;

    @GetMapping
    public List<EnderecoEntregaDTO> listar(@RequestParam Long empresaId) {
        return enderecoEntregaService.listarPorEmpresa(empresaId);
    }

    @PostMapping
    public ResponseEntity<EnderecoEntregaDTO> criar(@Valid @RequestBody EnderecoEntregaRequestDTO dto) {
        return ResponseEntity.status(201).body(enderecoEntregaService.criar(dto));
    }
}
