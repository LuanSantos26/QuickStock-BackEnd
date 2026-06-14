package com.quickstock.backend.repository;

import com.quickstock.backend.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByEmpresaIdAndAtivo(Long empresaId, Integer ativo);

    Optional<Produto> findByCodigo(String codigo);

    Optional<Produto> findByEmpresaIdAndCodigoOrigem(Long empresaId, String codigoOrigem);

    long countByEmpresaId(Long empresaId);
}
