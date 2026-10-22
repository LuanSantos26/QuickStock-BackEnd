package com.stock4event.backend.controller;

import com.stock4event.backend.dto.LogisticaResumoDTO;
import com.stock4event.backend.security.AcessoService;
import com.stock4event.backend.service.LogisticaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logistica")
@CrossOrigin(origins = "*")
public class LogisticaController {

    @Autowired private LogisticaService logisticaService;
    @Autowired private AcessoService acesso;

    @GetMapping
    public LogisticaResumoDTO resumo(@RequestParam(required = false) Long empresaId) {
        return logisticaService.resumo(acesso.resolverEmpresa(empresaId));
    }
}
