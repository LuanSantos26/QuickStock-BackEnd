package com.stock4event.backend.repository;

import com.stock4event.backend.entity.Produto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByEmpresaIdAndAtivo(Long empresaId, Integer ativo);

    Optional<Produto> findByCodigo(String codigo);

    Optional<Produto> findByEmpresaIdAndCodigoOrigem(Long empresaId, String codigoOrigem);

    long countByEmpresaId(Long empresaId);

    /** Produtos ativos de todas as contas fornecedoras, dos cadastrados mais recentemente. */
    @Query("""
            SELECT p FROM Produto p
            WHERE p.ativo = 1 AND p.empresa.tipo IN :tipos
            ORDER BY p.criadoEm DESC NULLS LAST, p.id DESC
            """)
    List<Produto> findRecentesDeFornecedores(@Param("tipos") List<String> tipos,
                                             org.springframework.data.domain.Pageable pagina);

    /** Lê o produto bloqueando a linha até o fim da transação (baixa de estoque concorrente). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Produto p WHERE p.id = :id")
    Optional<Produto> findByIdParaAtualizar(@Param("id") Long id);
}
