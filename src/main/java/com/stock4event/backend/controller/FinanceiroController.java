package com.stock4event.backend.controller;

import com.stock4event.backend.dto.FinanceiroResumoDTO;
import com.stock4event.backend.dto.StockDiaDTO;
import com.stock4event.backend.security.AcessoService;
import com.stock4event.backend.service.FinanceiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financeiro")
@CrossOrigin(origins = "*")
public class FinanceiroController {

    @Autowired private FinanceiroService financeiroService;
    @Autowired private AcessoService acesso;

    @GetMapping("/resumo")
    public FinanceiroResumoDTO resumo(@RequestParam(required = false) Long empresaCompradoraId) {
        return financeiroService.obterResumo(acesso.resolverEmpresa(empresaCompradoraId));
    }

    @GetMapping("/stock-dia")
    public StockDiaDTO stockDia(@RequestParam(required = false) Long empresaCompradoraId) {
        return financeiroService.obterStockDia(acesso.resolverEmpresa(empresaCompradoraId));
    }
}
