package com.quickstock.backend.repository;

import com.quickstock.backend.entity.SolicitacaoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacaoCompraRepository extends JpaRepository<SolicitacaoCompra, Long> {
    List<SolicitacaoCompra> findByEmpresaCompradoraIdOrderByCriadoEmDesc(Long empresaCompradoraId);
}
