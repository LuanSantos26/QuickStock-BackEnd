package com.stock4event.backend.controller;

import com.stock4event.backend.entity.Perfil;
import com.stock4event.backend.repository.PerfilRepository;
import com.stock4event.backend.security.AcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Perfis de acesso são dados de referência do sistema (seed "Admin").
 * Leitura liberada para usuários logados; criar, alterar ou apagar perfis exigiria
 * um administrador da plataforma, que o sistema não tem — por isso é recusado (403).
 */
@RestController
@RequestMapping("/api/perfis")
@CrossOrigin(origins = "*")
public class PerfilController {

    private static final String SOMENTE_LEITURA = "Perfis de acesso são definidos pelo sistema e não podem ser alterados.";

    @Autowired
    private PerfilRepository repository;

    @GetMapping
    public List<Perfil> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Perfil> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Perfil> criar() {
        throw AcessoService.proibido(SOMENTE_LEITURA);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Perfil> atualizar(@PathVariable Long id) {
        throw AcessoService.proibido(SOMENTE_LEITURA);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        throw AcessoService.proibido(SOMENTE_LEITURA);
    }
}
