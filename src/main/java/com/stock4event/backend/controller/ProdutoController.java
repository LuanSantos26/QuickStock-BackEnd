package com.stock4event.backend.controller;

import com.stock4event.backend.dto.ProdutoRequestDTO;
import com.stock4event.backend.dto.ProdutoResponseDTO;
import com.stock4event.backend.entity.Empresa;
import com.stock4event.backend.entity.Produto;
import com.stock4event.backend.repository.EmpresaRepository;
import com.stock4event.backend.repository.ProdutoRepository;
import com.stock4event.backend.security.AcessoService;
import com.stock4event.backend.service.EstoqueProdutoService;
import com.stock4event.backend.service.ProdutoService;
import com.stock4event.backend.service.ProdutoUploadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

    @Autowired private ProdutoRepository repository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private ProdutoUploadService uploadService;
    @Autowired private ProdutoService produtoService;
    @Autowired private EstoqueProdutoService estoqueProdutoService;
    @Autowired private AcessoService acesso;

    /** Mesmos tipos que o marketplace exibe como fornecedores. */
    private static final List<String> TIPOS_FORNECEDOR = List.of("DISTRIBUIDOR", "AMBOS");

    /** Produtos ativos da empresa do usuário logado. */
    @GetMapping
    public List<ProdutoResponseDTO> listar(@RequestParam(required = false) Long empresaId) {
        return repository.findByEmpresaIdAndAtivo(acesso.resolverEmpresa(empresaId), 1)
                .stream()
                .map(ProdutoResponseDTO::new)
                .toList();
    }

    /**
     * Produto da própria empresa, ou produto ativo de um fornecedor (o mesmo que o marketplace
     * já mostra a qualquer usuário logado). Estoque interno de outras empresas não é exposto.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(produto -> {
                    boolean proprio = produto.getEmpresa().getId().equals(acesso.empresaId());
                    boolean catalogoPublico = Integer.valueOf(1).equals(produto.getAtivo())
                            && TIPOS_FORNECEDOR.contains(produto.getEmpresa().getTipo());
                    if (!proprio && !catalogoPublico) {
                        throw AcessoService.proibido(AcessoService.EMPRESA_DIFERENTE);
                    }
                    return ResponseEntity.ok(new ProdutoResponseDTO(produto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Arquivo de imagem é obrigatório."));
        }

        try {
            String imagemUrl = uploadService.salvarImagem(file);
            return ResponseEntity.ok(Map.of("imagemUrl", imagemUrl));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Não foi possível salvar a imagem."));
        }
    }

    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody ProdutoRequestDTO dto) {
        var empresa = empresaRepository.findById(acesso.resolverEmpresa(dto.getEmpresaId()));
        if (empresa.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Empresa não encontrada."));
        }

        Produto produto = mapToEntity(dto, empresa.get());
        produto.setAtivo(1);
        Produto salvo = repository.save(produto);
        return ResponseEntity.status(201).body(new ProdutoResponseDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @Valid @RequestBody ProdutoRequestDTO dto) {
        var produtoOpt = repository.findById(id);
        if (produtoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Produto produto = produtoOpt.get();
        acesso.resolverEmpresa(dto.getEmpresaId());
        acesso.exigirEmpresa(produto.getEmpresa().getId());

        produto.setNome(dto.getNome());
        produto.setPrecoVenda(dto.getPrecoVenda());
        produto.setUnidade(dto.getUnidade());
        produto.setDescricao(dto.getDescricao());
        produto.setImagemUrl(dto.getImagemUrl());
        if (dto.getEstoque() != null) {
            produto.setEstoque(dto.getEstoque());
        }
        return ResponseEntity.ok(new ProdutoResponseDTO(repository.save(produto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> desativar(@PathVariable Long id,
                                                         @RequestParam(required = false) Long empresaId) {
        produtoService.desativar(id, acesso.resolverEmpresa(empresaId));
        return ResponseEntity.ok(Map.of("mensagem", "Produto removido."));
    }

    private Produto mapToEntity(ProdutoRequestDTO dto, Empresa empresa) {
        Produto produto = new Produto();
        produto.setEmpresa(empresa);
        produto.setNome(dto.getNome());
        produto.setPrecoVenda(dto.getPrecoVenda());
        produto.setUnidade(dto.getUnidade());
        produto.setDescricao(dto.getDescricao());
        produto.setImagemUrl(dto.getImagemUrl());
        produto.setEstoque(dto.getEstoque() != null ? dto.getEstoque() : java.math.BigDecimal.ZERO);
        produto.setCodigo(estoqueProdutoService.gerarCodigoCatalogo(empresa.getId()));
        return produto;
    }
}
