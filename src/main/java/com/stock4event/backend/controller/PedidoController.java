package com.stock4event.backend.controller;

import com.stock4event.backend.entity.Barraca;
import com.stock4event.backend.entity.ItemPedido;
import com.stock4event.backend.entity.Pedido;
import com.stock4event.backend.exception.CadastroException;
import com.stock4event.backend.repository.BarracaRepository;
import com.stock4event.backend.repository.ItemPedidoRepository;
import com.stock4event.backend.repository.PedidoRepository;
import com.stock4event.backend.repository.UsuarioRepository;
import com.stock4event.backend.security.AcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Pedidos de PDV (venda na barraca) e consulta de pedidos da empresa.
 * Operador, empresa, tipo, status inicial e valor total são definidos pelo servidor.
 */
@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    @Autowired private PedidoRepository     pedidoRepository;
    @Autowired private ItemPedidoRepository itemRepository;
    @Autowired private BarracaRepository    barracaRepository;
    @Autowired private UsuarioRepository    usuarioRepository;
    @Autowired private AcessoService acesso;

    @GetMapping
    public List<Pedido> listar(@RequestParam(required = false) Long barracaId,
                                @RequestParam(required = false) String status) {
        if (barracaId != null) {
            Barraca barraca = barracaRepository.findById(barracaId)
                    .orElseThrow(() -> new CadastroException(HttpStatus.NOT_FOUND, "Barraquinha não encontrada."));
            acesso.exigirEmpresa(AcessoService.empresaDaBarraca(barraca));
            return status != null
                    ? pedidoRepository.findByBarracaIdAndStatus(barracaId, status)
                    : pedidoRepository.findByBarracaId(barracaId);
        }
        return pedidoRepository.findAll().stream()
                .filter(acesso::podeVerPedido)
                .filter(p -> status == null || status.equals(p.getStatus()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscar(@PathVariable Long id) {
        return pedidoRepository.findById(id)
                .map(p -> {
                    acesso.exigirVerPedido(p);
                    return ResponseEntity.ok(p);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/itens")
    public ResponseEntity<List<ItemPedido>> listarItens(@PathVariable Long id) {
        Pedido pedido = pedidoRepository.findById(id).orElse(null);
        if (pedido == null) return ResponseEntity.notFound().build();
        acesso.exigirVerPedido(pedido);
        return ResponseEntity.ok(itemRepository.findByPedidoId(id));
    }

    /** Abre um pedido de PDV numa barraca da empresa, com o usuário logado como operador. */
    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody Pedido dados) {
        if (dados.getBarraca() == null || dados.getBarraca().getId() == null) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Informe a barraquinha do pedido.");
        }
        Barraca barraca = barracaRepository.findById(dados.getBarraca().getId())
                .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Barraquinha não encontrada."));
        acesso.exigirEmpresa(AcessoService.empresaDaBarraca(barraca));
        validarTamanho(dados.getObservacao(), 500, "Observação");
        validarTamanho(dados.getMetodoPagamento(), 20, "Forma de pagamento");

        Pedido pedido = new Pedido();
        pedido.setTipo("pdv");
        pedido.setBarraca(barraca);
        pedido.setOperador(usuarioRepository.findById(acesso.usuarioId())
                .orElseThrow(() -> new CadastroException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado.")));
        pedido.setObservacao(dados.getObservacao());
        pedido.setMetodoPagamento(dados.getMetodoPagamento());
        pedido.setStatus("aberto");
        pedido.setValorTotal(BigDecimal.ZERO);
        return ResponseEntity.status(201).body(pedidoRepository.save(pedido));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Pedido> atualizarStatus(@PathVariable Long id,
                                                   @RequestBody Map<String, String> body) {
        return pedidoRepository.findById(id).map(p -> {
            acesso.exigirPedidoPdvProprio(p);
            p.setStatus(statusValido(body.get("status")));
            return ResponseEntity.ok(pedidoRepository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Atualiza o status. O valor total é calculado pelos itens e não é aceito do cliente. */
    @PutMapping("/{id}")
    public ResponseEntity<Pedido> atualizar(@PathVariable Long id,
                                             @RequestBody Map<String, Object> dados) {
        return pedidoRepository.findById(id).map(p -> {
            acesso.exigirPedidoPdvProprio(p);
            Object status = dados.get("status");
            p.setStatus(statusValido(status != null ? status.toString() : null));
            return ResponseEntity.ok(pedidoRepository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        Pedido pedido = pedidoRepository.findById(id).orElse(null);
        if (pedido == null) return ResponseEntity.notFound().build();
        acesso.exigirPedidoPdvProprio(pedido);
        pedido.setStatus("cancelado");
        pedidoRepository.save(pedido);
        return ResponseEntity.noContent().build();
    }

    private String statusValido(String status) {
        if (status == null || status.isBlank()) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Informe o status do pedido.");
        }
        validarTamanho(status.trim(), 20, "Status");
        return status.trim();
    }

    private void validarTamanho(String valor, int max, String campo) {
        if (valor != null && valor.length() > max) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, campo + " deve ter no máximo " + max + " caracteres.");
        }
    }
}
