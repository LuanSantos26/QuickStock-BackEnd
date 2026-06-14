package com.quickstock.backend.service;

import com.quickstock.backend.dto.FornecedorResponseDTO;
import com.quickstock.backend.dto.ProdutoResponseDTO;
import com.quickstock.backend.entity.Empresa;
import com.quickstock.backend.exception.CadastroException;
import com.quickstock.backend.repository.EmpresaRepository;
import com.quickstock.backend.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketplaceService {

    private static final List<String> TIPOS_FORNECEDOR = List.of("DISTRIBUIDOR");

    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private ProdutoRepository produtoRepository;

    public List<FornecedorResponseDTO> listarFornecedores(Long empresaCompradoraId) {
        Long excluirId = empresaCompradoraId != null ? empresaCompradoraId : -1L;
        return empresaRepository.findByTipoInAndIdNot(TIPOS_FORNECEDOR, excluirId).stream()
                .map(this::toFornecedorDTO)
                .toList();
    }

    public List<ProdutoResponseDTO> listarProdutosFornecedor(Long fornecedorId) {
        Empresa fornecedor = empresaRepository.findById(fornecedorId)
                .orElseThrow(() -> new CadastroException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado."));

        if (!"DISTRIBUIDOR".equals(fornecedor.getTipo())) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Empresa informada não é fornecedora.");
        }

        return produtoRepository.findByEmpresaIdAndAtivo(fornecedorId, 1).stream()
                .map(ProdutoResponseDTO::new)
                .toList();
    }

    private FornecedorResponseDTO toFornecedorDTO(Empresa empresa) {
        int total = produtoRepository.findByEmpresaIdAndAtivo(empresa.getId(), 1).size();
        return new FornecedorResponseDTO(empresa, total);
    }
}
