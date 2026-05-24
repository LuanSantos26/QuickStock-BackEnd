package com.quickstock.backend.controller;

import com.quickstock.backend.entity.Produto;
import com.quickstock.backend.repository.EmpresaRepository;
import com.quickstock.backend.repository.ProdutoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

    @Autowired private ProdutoRepository  repository;
    @Autowired private EmpresaRepository  empresaRepository;

    @GetMapping
    public List<Produto> listar(@RequestParam(required = false) Long empresaId) {
        if (empresaId != null) return repository.findByEmpresaIdAndAtivo(empresaId, 1);
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody Produto produto) {
        if (produto.getEmpresa() == null || produto.getEmpresa().getId() == null) {
            return ResponseEntity.badRequest().body("empresaId é obrigatório.");
        }
        empresaRepository.findById(produto.getEmpresa().getId())
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));
        return ResponseEntity.status(201).body(repository.save(produto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id,
                                              @Valid @RequestBody Produto dados) {
        return repository.findById(id).map(p -> {
            p.setNome(dados.getNome());
            p.setPrecoVenda(dados.getPrecoVenda());
            p.setUnidade(dados.getUnidade());
            p.setAtivo(dados.getAtivo());
            return ResponseEntity.ok(repository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.findById(id).ifPresent(p -> {
            p.setAtivo(0);
            repository.save(p);
        });
        return ResponseEntity.noContent().build();
    }
}
