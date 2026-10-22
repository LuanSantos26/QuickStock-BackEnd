package com.stock4event.backend.controller;

import com.stock4event.backend.dto.CaminhoneiroRequestDTO;
import com.stock4event.backend.dto.CaminhoneiroResponseDTO;
import com.stock4event.backend.security.AcessoService;
import com.stock4event.backend.service.LogisticaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/caminhoneiros")
@CrossOrigin(origins = "*")
public class CaminhoneiroController {

    @Autowired private LogisticaService logisticaService;
    @Autowired private AcessoService acesso;

    @GetMapping
    public List<CaminhoneiroResponseDTO> listar(@RequestParam(required = false) Long empresaId) {
        return logisticaService.listar(acesso.resolverEmpresa(empresaId));
    }

    @PostMapping
    public ResponseEntity<CaminhoneiroResponseDTO> criar(@Valid @RequestBody CaminhoneiroRequestDTO dto) {
        dto.setEmpresaId(acesso.resolverEmpresa(dto.getEmpresaId()));
        return ResponseEntity.status(201).body(logisticaService.criar(dto));
    }
}
