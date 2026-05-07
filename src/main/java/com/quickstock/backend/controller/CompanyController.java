package com.quickstock.backend.controller;

import com.quickstock.backend.model.Company;
import com.quickstock.backend.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = "*") // Permite que o App Mobile acesse a API
public class CompanyController {

    @Autowired
    private CompanyRepository repository;

    @PostMapping("/register")
    public Company createCompany(@RequestBody Company company) {
        // Apenas devolve o que recebeu para confirmar que a conexão funcionou
        System.out.println("Recebido: " + company.getName());
        return company;
    }
}