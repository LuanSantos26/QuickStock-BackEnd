package com.stock4event.backend.controller;

import com.stock4event.backend.dto.EmpresaRequestDTO;
import com.stock4event.backend.entity.Empresa;
import com.stock4event.backend.exception.CadastroException;
import com.stock4event.backend.repository.EmpresaRepository;
import com.stock4event.backend.security.AcessoService;
import com.stock4event.backend.service.ProdutoUploadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
    @Autowired private ProdutoUploadService uploadService;

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

    /** Envia a foto (logo) ou a capa da loja; devolve a URL para usar no PUT. */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Arquivo de imagem é obrigatório."));
        }

        try {
            String imagemUrl = uploadService.salvarImagem(file, "empresas");
            return ResponseEntity.ok(Map.of("imagemUrl", imagemUrl));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Não foi possível salvar a imagem."));
        }
    }

    /**
     * Altera nome, CNPJ, telefone e o perfil da loja (descrição, logo e capa) da própria empresa.
     * O tipo não é alterado aqui. Logo e capa só aceitam imagens enviadas por /upload
     * (caminhos /uploads/...); URLs externas são ignoradas.
     */
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
            if (dados.getDescricao() != null) {
                emp.setDescricao(vazioParaNull(dados.getDescricao().trim()));
            }
            if (ProdutoUploadService.caminhoImagemValido(dados.getLogoUrl())) {
                emp.setLogoUrl(vazioParaNull(dados.getLogoUrl().trim()));
            }
            if (ProdutoUploadService.caminhoImagemValido(dados.getCapaUrl())) {
                emp.setCapaUrl(vazioParaNull(dados.getCapaUrl().trim()));
            }
            return ResponseEntity.ok(repository.save(emp));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        throw AcessoService.proibido(SEM_ADMIN_PLATAFORMA);
    }

    private static String vazioParaNull(String valor) {
        return valor.isEmpty() ? null : valor;
    }
}
