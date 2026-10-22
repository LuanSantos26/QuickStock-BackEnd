package com.stock4event.backend.controller;

import com.stock4event.backend.entity.ItemPedido;
import com.stock4event.backend.entity.Pedido;
import com.stock4event.backend.entity.Produto;
import com.stock4event.backend.exception.CadastroException;
import com.stock4event.backend.repository.ItemPedidoRepository;
import com.stock4event.backend.repository.PedidoRepository;
import com.stock4event.backend.repository.ProdutoRepository;
import com.stock4event.backend.security.AcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/itens-pedido")
@CrossOrigin(origins = "*")
public class ItemPedidoController {

    @Autowired private ItemPedidoRepository repository;
    @Autowired private PedidoRepository     pedidoRepository;
    @Autowired private ProdutoRepository    produtoRepository;
    @Autowired private AcessoService acesso;

    @GetMapping
    public List<ItemPedido> listar(@RequestParam(required = false) Long pedidoId) {
        if (pedidoId != null) {
            Pedido pedido = pedidoRepository.findById(pedidoId)
                    .orElseThrow(() -> new CadastroException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
            acesso.exigirVerPedido(pedido);
            return repository.findByPedidoId(pedidoId);
        }
        return repository.findAll().stream()
                .filter(i -> acesso.podeVerPedido(i.getPedido()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemPedido> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(i -> {
                    acesso.exigirVerPedido(i.getPedido());
                    return ResponseEntity.ok(i);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Adiciona um produto da própria empresa a um pedido de PDV da própria barraca. O subtotal é calculado aqui. */
    @PostMapping
    @Transactional
    public ResponseEntity<ItemPedido> criar(@RequestBody ItemPedido dados) {
        if (dados.getPedido() == null || dados.getPedido().getId() == null) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Informe o pedido.");
        }
        if (dados.getProduto() == null || dados.getProduto().getId() == null) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Informe o produto.");
        }
        if (dados.getQuantidade() == null || dados.getQuantidade().compareTo(new BigDecimal("0.001")) < 0) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Quantidade deve ser maior que zero.");
        }
        if (dados.getPrecoUnitario() == null || dados.getPrecoUnitario().signum() < 0) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Preço unitário não pode ser negativo.");
        }

        Pedido pedido = pedidoRepository.findById(dados.getPedido().getId())
                .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Pedido não encontrado."));
        acesso.exigirPedidoPdvProprio(pedido);
        Produto produto = produtoRepository.findById(dados.getProduto().getId())
                .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Produto não encontrado."));
        acesso.exigirEmpresa(produto.getEmpresa().getId());

        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(dados.getQuantidade());
        item.setPrecoUnitario(dados.getPrecoUnitario());
        item.setSubtotal(dados.getPrecoUnitario().multiply(dados.getQuantidade()));
        ItemPedido salvo = repository.save(item);

        recalcularTotal(pedido.getId());
        return ResponseEntity.status(201).body(salvo);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        ItemPedido item = repository.findById(id).orElse(null);
        if (item == null) return ResponseEntity.notFound().build();
        acesso.exigirPedidoPdvProprio(item.getPedido());
        Long pedidoId = item.getPedido().getId();
        repository.delete(item);
        recalcularTotal(pedidoId);
        return ResponseEntity.noContent().build();
    }

    private void recalcularTotal(Long pedidoId) {
        pedidoRepository.findById(pedidoId).ifPresent(pedido -> {
            BigDecimal total = repository.findByPedidoId(pedidoId)
                    .stream()
                    .map(ItemPedido::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            pedido.setValorTotal(total);
            pedidoRepository.save(pedido);
        });
    }
}
