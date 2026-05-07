package com.quickstock.backend.repository;

import com.quickstock.backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    // Aqui você já ganha métodos como save(), findAll(), delete() de graça!
}