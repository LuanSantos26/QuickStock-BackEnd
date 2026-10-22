package com.stock4event.backend.controller;

import com.stock4event.backend.dto.ProdutoPdvDTO;
import com.stock4event.backend.dto.VendaPdvRequestDTO;
import com.stock4event.backend.dto.VendaPdvResponseDTO;
import com.stock4event.backend.exception.CadastroException;
import com.stock4event.backend.security.AcessoService;
import com.stock4event.backend.service.VendaPdvService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PDV: busca de produtos do ponto de venda e registro de vendas. Exige login (Spring Security)
 * e só enxerga quiosques, produtos e vendas da empresa do usuário logado.
 */
@RestController
@RequestMapping("/api/pdv")
@CrossOrigin(origins = "*")
public class PdvController {

    @Autowired private VendaPdvService vendaPdvService;
    @Autowired private AcessoService acesso;

    @GetMapping("/produtos")
    public List<ProdutoPdvDTO> buscarProdutos(@RequestParam Long barracaId,
                                              @RequestParam(required = false) String busca) {
        if (busca != null && busca.length() > 100) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Busca deve ter no máximo 100 caracteres.");
        }
        return vendaPdvService.buscarProdutos(barracaId, acesso.empresaId(), busca);
    }

    /**
     * 201 com a venda registrada. Reenviar a mesma chaveVenda (clique repetido, rede instável)
     * devolve 200 com a venda já existente, sem registrar outra nem baixar estoque de novo.
     */
    @PostMapping("/vendas")
    public ResponseEntity<VendaPdvResponseDTO> finalizar(@Valid @RequestBody VendaPdvRequestDTO dto) {
        Long empresaId = acesso.empresaId();
        String chave = dto.getChaveVenda().trim();
        var jaRegistrada = vendaPdvService.buscarPorChave(chave, empresaId);
        if (jaRegistrada.isPresent()) {
            return ResponseEntity.ok(jaRegistrada.get());
        }
        try {
            return ResponseEntity.status(201).body(vendaPdvService.finalizar(dto, empresaId, acesso.usuarioId()));
        } catch (DataIntegrityViolationException ex) {
            // Duas requisições simultâneas com a mesma chave: a segunda perde na chave única e devolve a primeira.
            return vendaPdvService.buscarPorChave(chave, empresaId)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> ex);
        }
    }

    @GetMapping("/vendas/{id}")
    public VendaPdvResponseDTO buscarVenda(@PathVariable Long id) {
        return vendaPdvService.buscarVenda(id, acesso.empresaId());
    }
}
