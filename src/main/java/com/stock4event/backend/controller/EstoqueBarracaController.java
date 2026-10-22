package com.stock4event.backend.controller;

import com.stock4event.backend.entity.Barraca;
import com.stock4event.backend.entity.EstoqueBarraca;
import com.stock4event.backend.entity.Produto;
import com.stock4event.backend.exception.CadastroException;
import com.stock4event.backend.repository.BarracaRepository;
import com.stock4event.backend.repository.EstoqueBarracaRepository;
import com.stock4event.backend.repository.ProdutoRepository;
import com.stock4event.backend.security.AcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estoque-barraca")
@CrossOrigin(origins = "*")
public class EstoqueBarracaController {

    @Autowired private EstoqueBarracaRepository repository;
    @Autowired private BarracaRepository barracaRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private AcessoService acesso;

    @GetMapping
    public List<EstoqueBarraca> listar(@RequestParam(required = false) Long barracaId) {
        if (barracaId != null) {
            exigirBarracaPropria(barracaId);
            return repository.findByBarracaId(barracaId);
        }
        Long empresaId = acesso.empresaId();
        return repository.findAll().stream()
                .filter(e -> empresaId.equals(AcessoService.empresaDaBarraca(e.getBarraca())))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstoqueBarraca> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(e -> {
                    acesso.exigirEmpresa(AcessoService.empresaDaBarraca(e.getBarraca()));
                    return ResponseEntity.ok(e);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/barraca/{barracaId}/produto/{produtoId}")
    public ResponseEntity<EstoqueBarraca> buscarSaldo(@PathVariable Long barracaId,
                                                       @PathVariable Long produtoId) {
        exigirBarracaPropria(barracaId);
        return repository.findByBarracaIdAndProdutoId(barracaId, produtoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Cria a linha de estoque de um produto da empresa numa barraca da empresa. */
    @PostMapping
    public ResponseEntity<EstoqueBarraca> criar(@RequestBody EstoqueBarraca dados) {
        if (dados.getBarraca() == null || dados.getBarraca().getId() == null) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Informe a barraquinha.");
        }
        if (dados.getProduto() == null || dados.getProduto().getId() == null) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Informe o produto.");
        }
        BigDecimal quantidade = quantidadeValida(dados.getQuantidade());
        Barraca barraca = exigirBarracaPropria(dados.getBarraca().getId());
        Produto produto = produtoRepository.findById(dados.getProduto().getId())
                .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Produto não encontrado."));
        acesso.exigirEmpresa(produto.getEmpresa().getId());
        if (repository.findByBarracaIdAndProdutoId(barraca.getId(), produto.getId()).isPresent()) {
            throw new CadastroException(HttpStatus.CONFLICT, "Este produto já tem estoque nesta barraquinha.");
        }

        EstoqueBarraca estoque = new EstoqueBarraca();
        estoque.setBarraca(barraca);
        estoque.setProduto(produto);
        estoque.setQuantidade(quantidade);
        return ResponseEntity.status(201).body(repository.save(estoque));
    }

    @PatchMapping("/{id}/quantidade")
    public ResponseEntity<EstoqueBarraca> ajustarQuantidade(@PathVariable Long id,
                                                             @RequestBody Map<String, Object> body) {
        return repository.findById(id).map(e -> {
            acesso.exigirEmpresa(AcessoService.empresaDaBarraca(e.getBarraca()));
            Object valor = body.get("quantidade");
            BigDecimal quantidade;
            try {
                quantidade = valor == null ? null : new BigDecimal(valor.toString());
            } catch (NumberFormatException ex) {
                throw new CadastroException(HttpStatus.BAD_REQUEST, "Quantidade inválida.");
            }
            e.setQuantidade(quantidadeValida(quantidade));
            return ResponseEntity.ok(repository.save(e));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        EstoqueBarraca estoque = repository.findById(id).orElse(null);
        if (estoque == null) return ResponseEntity.notFound().build();
        acesso.exigirEmpresa(AcessoService.empresaDaBarraca(estoque.getBarraca()));
        repository.delete(estoque);
        return ResponseEntity.noContent().build();
    }

    private Barraca exigirBarracaPropria(Long barracaId) {
        Barraca barraca = barracaRepository.findById(barracaId)
                .orElseThrow(() -> new CadastroException(HttpStatus.NOT_FOUND, "Barraquinha não encontrada."));
        acesso.exigirEmpresa(AcessoService.empresaDaBarraca(barraca));
        return barraca;
    }

    private BigDecimal quantidadeValida(BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() < 0) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Quantidade deve ser zero ou maior.");
        }
        return quantidade;
    }
}
