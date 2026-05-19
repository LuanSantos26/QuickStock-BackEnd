package com.quickstock.backend.service;

import com.quickstock.backend.model.Company;
import com.quickstock.backend.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository repository;

    public Company cadastrar(Company company) {
        return repository.save(company);
    }
}