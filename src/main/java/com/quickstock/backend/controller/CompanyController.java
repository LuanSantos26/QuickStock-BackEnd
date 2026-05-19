package com.quickstock.backend.controller;

import com.quickstock.backend.model.Company;
import com.quickstock.backend.service.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/empresas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService service;

    @PostMapping
    public ResponseEntity<Company> cadastrar(@RequestBody Company company) {
        Company salvo = service.cadastrar(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }
}