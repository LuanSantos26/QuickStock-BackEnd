package com.quickstock.backend.repository;

import com.quickstock.backend.entity.Barraca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarracaRepository extends JpaRepository<Barraca, Long> {
    List<Barraca> findByEventoId(Long eventoId);
}
