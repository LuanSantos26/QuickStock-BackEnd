package com.quickstock.backend.repository;

import com.quickstock.backend.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByBarracaId(Long barracaId);
    List<Pedido> findByBarracaIdAndStatus(Long barracaId, String status);
}
