package com.stock4event.backend.controller;

import com.stock4event.backend.entity.Pagamento;
import com.stock4event.backend.entity.Pedido;
import com.stock4event.backend.exception.CadastroException;
import com.stock4event.backend.repository.PagamentoRepository;
import com.stock4event.backend.repository.PedidoRepository;
import com.stock4event.backend.security.AcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/pagamentos")
@CrossOrigin(origins = "*")
public class PagamentoController {

    /** Status usados pelo sistema (SolicitacaoCompraService e PagamentoController). */
    private static final Set<String> STATUS_VALIDOS = Set.of("pendente", "aprovado", "estornado", "cancelado");

    @Autowired private PagamentoRepository repository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private AcessoService acesso;

    @GetMapping
    public List<Pagamento> listar(@RequestParam(required = false) Long pedidoId) {
        if (pedidoId != null) {
            Pedido pedido = pedidoRepository.findById(pedidoId)
                    .orElseThrow(() -> new CadastroException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
            acesso.exigirVerPedido(pedido);
            return repository.findByPedidoId(pedidoId);
        }
        return repository.findAll().stream()
                .filter(p -> acesso.podeVerPedido(p.getPedido()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pagamento> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(p -> {
                    acesso.exigirVerPedido(p.getPedido());
                    return ResponseEntity.ok(p);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Registra um pagamento (sempre "pendente") num pedido de PDV da própria barraca. */
    @PostMapping
    public ResponseEntity<Pagamento> criar(@RequestBody Pagamento dados) {
        if (dados.getPedido() == null || dados.getPedido().getId() == null) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Informe o pedido.");
        }
        if (dados.getMetodo() == null || dados.getMetodo().isBlank() || dados.getMetodo().length() > 20) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Informe o método de pagamento (até 20 caracteres).");
        }
        if (dados.getValor() == null || dados.getValor().compareTo(new BigDecimal("0.01")) < 0) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Valor deve ser de pelo menos R$ 0,01.");
        }
        if (dados.getReferenciaPagamento() != null && dados.getReferenciaPagamento().length() > 120) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Referência deve ter no máximo 120 caracteres.");
        }
        Pedido pedido = pedidoRepository.findById(dados.getPedido().getId())
                .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Pedido não encontrado."));
        acesso.exigirPedidoPdvProprio(pedido);

        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setMetodo(dados.getMetodo().trim());
        pagamento.setValor(dados.getValor());
        pagamento.setReferenciaPagamento(dados.getReferenciaPagamento());
        pagamento.setStatus("pendente");
        return ResponseEntity.status(201).body(repository.save(pagamento));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Pagamento> atualizarStatus(@PathVariable Long id,
                                                      @RequestBody Map<String, String> body) {
        return repository.findById(id).map(pg -> {
            acesso.exigirPedidoPdvProprio(pg.getPedido());
            String status = body.get("status") != null ? body.get("status").trim().toLowerCase() : "";
            if (!STATUS_VALIDOS.contains(status)) {
                throw new CadastroException(HttpStatus.BAD_REQUEST, "Status inválido. Use: " + STATUS_VALIDOS);
            }
            pg.setStatus(status);
            return ResponseEntity.ok(repository.save(pg));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        Pagamento pagamento = repository.findById(id).orElse(null);
        if (pagamento == null) return ResponseEntity.notFound().build();
        acesso.exigirPedidoPdvProprio(pagamento.getPedido());
        repository.delete(pagamento);
        return ResponseEntity.noContent().build();
    }
}
