package com.quickstock.backend.controller;

import com.quickstock.backend.dto.FinanceiroResumoDTO;
import com.quickstock.backend.service.FinanceiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financeiro")
@CrossOrigin(origins = "*")
public class FinanceiroController {

    @Autowired private FinanceiroService financeiroService;

    @GetMapping("/resumo")
    public FinanceiroResumoDTO resumo(@RequestParam Long empresaCompradoraId) {
        return financeiroService.obterResumo(empresaCompradoraId);
    }
}
