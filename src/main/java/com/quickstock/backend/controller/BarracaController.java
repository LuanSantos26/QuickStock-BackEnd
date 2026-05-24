package com.quickstock.backend.controller;

import com.quickstock.backend.entity.Barraca;
import com.quickstock.backend.repository.BarracaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/barracas")
@CrossOrigin(origins = "*")
public class BarracaController {

    @Autowired private BarracaRepository repository;

    @GetMapping
    public List<Barraca> listar(@RequestParam(required = false) Long eventoId) {
        if (eventoId != null) return repository.findByEventoId(eventoId);
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Barraca> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Barraca> criar(@Valid @RequestBody Barraca barraca) {
        return ResponseEntity.status(201).body(repository.save(barraca));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Barraca> atualizar(@PathVariable Long id,
                                              @Valid @RequestBody Barraca dados) {
        return repository.findById(id).map(b -> {
            b.setNome(dados.getNome());
            b.setAtiva(dados.getAtiva());
            b.setResponsavel(dados.getResponsavel());
            return ResponseEntity.ok(repository.save(b));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
