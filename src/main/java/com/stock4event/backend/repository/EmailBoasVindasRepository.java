package com.stock4event.backend.repository;

import com.stock4event.backend.entity.EmailBoasVindas;
import com.stock4event.backend.entity.EmailBoasVindas.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailBoasVindasRepository extends JpaRepository<EmailBoasVindas, Long> {

    Optional<EmailBoasVindas> findByUsuarioId(Long usuarioId);

    /**
     * Reserva o registro para uma tentativa de envio. A troca PENDENTE → ENVIANDO é um único
     * UPDATE condicional: se duas execuções tentarem ao mesmo tempo, só uma recebe 1 e envia.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update EmailBoasVindas e
               set e.status = :enviando, e.tentativas = e.tentativas + 1, e.atualizadoEm = :agora
             where e.id = :id and e.status = :pendente and e.proximaTentativaEm <= :agora
            """)
    int reservarParaEnvio(@Param("id") Long id,
                          @Param("agora") LocalDateTime agora,
                          @Param("pendente") Status pendente,
                          @Param("enviando") Status enviando);

    @Query("""
            select e.id from EmailBoasVindas e
             where e.status = :pendente and e.proximaTentativaEm <= :agora
             order by e.proximaTentativaEm
            """)
    List<Long> idsProntosParaEnvio(@Param("agora") LocalDateTime agora,
                                   @Param("pendente") Status pendente,
                                   Pageable limite);

    /** Tentativas que ficaram presas em ENVIANDO (ex.: o servidor caiu no meio do envio). */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update EmailBoasVindas e
               set e.status = :indeterminado, e.atualizadoEm = :agora,
                   e.ultimoErro = 'Tentativa interrompida sem confirmação do servidor de e-mail.'
             where e.status = :enviando and e.atualizadoEm < :limite
            """)
    int marcarTravadosComoIndeterminados(@Param("limite") LocalDateTime limite,
                                         @Param("agora") LocalDateTime agora,
                                         @Param("enviando") Status enviando,
                                         @Param("indeterminado") Status indeterminado);
}
