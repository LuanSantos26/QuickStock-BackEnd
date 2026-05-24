package com.quickstock.backend.repository;

import com.quickstock.backend.entity.EstoqueBarraca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstoqueBarracaRepository extends JpaRepository<EstoqueBarraca, Long> {
    List<EstoqueBarraca> findByBarracaId(Long barracaId);
    Optional<EstoqueBarraca> findByBarracaIdAndProdutoId(Long barracaId, Long produtoId);
}
