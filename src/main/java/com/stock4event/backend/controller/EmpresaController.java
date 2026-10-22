package com.stock4event.backend.controller;

import com.stock4event.backend.dto.EmpresaRequestDTO;
import com.stock4event.backend.entity.Empresa;
import com.stock4event.backend.exception.CadastroException;
import com.stock4event.backend.repository.EmpresaRepository;
import com.stock4event.backend.security.AcessoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cada usuário enxerga e edita apenas a própria empresa. Empresas são criadas pelo
 * cadastro (POST /api/cadastro); não existe perfil de administrador da plataforma,
 * por isso criar e apagar empresas por aqui é recusado (403).
 */
@RestController
@RequestMapping("/api/empresas")
@CrossOrigin(origins = "*")
public class EmpresaController {

    private static final String SEM_ADMIN_PLATAFORMA =
            "Operação não permitida: empresas são criadas pelo cadastro de conta.";

    @Autowired private EmpresaRepository repository;
    @Autowired private AcessoService acesso;

    @GetMapping
    public List<Empresa> listar() {
        return repository.findById(acesso.empresaId()).map(List::of).orElse(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(emp -> {
                    acesso.exigirEmpresa(emp.getId());
                    return ResponseEntity.ok(emp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Empresa> criar() {
        throw AcessoService.proibido(SEM_ADMIN_PLATAFORMA);
    }

    /** Altera nome, CNPJ e telefone da própria empresa. Tipo, logo e capa não são alterados aqui. */
    @PutMapping("/{id}")
    public ResponseEntity<Empresa> atualizar(@PathVariable Long id,
                                              @Valid @RequestBody EmpresaRequestDTO dados) {
        return repository.findById(id).map(emp -> {
            acesso.exigirEmpresa(emp.getId());
            String cnpj = dados.getCnpj().trim();
            if (!cnpj.equals(emp.getCnpj()) && repository.existsByCnpj(cnpj)) {
                throw new CadastroException(HttpStatus.CONFLICT, "CNPJ já cadastrado.");
            }
            emp.setNome(dados.getNome().trim());
            emp.setCnpj(cnpj);
            emp.setTelefone(dados.getTelefone() != null ? dados.getTelefone().trim() : null);
            return ResponseEntity.ok(repository.save(emp));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        throw AcessoService.proibido(SEM_ADMIN_PLATAFORMA);
    }
}
