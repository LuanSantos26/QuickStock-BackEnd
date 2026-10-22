package com.stock4event.backend.service;

import com.stock4event.backend.dto.FornecedorResponseDTO;
import com.stock4event.backend.dto.ProdutoRecenteDTO;
import com.stock4event.backend.dto.ProdutoResponseDTO;
import com.stock4event.backend.dto.QuiosqueDisponivelDTO;
import com.stock4event.backend.entity.Barraca;
import com.stock4event.backend.entity.Empresa;
import com.stock4event.backend.entity.EstoqueBarraca;
import com.stock4event.backend.exception.CadastroException;
import com.stock4event.backend.repository.BarracaRepository;
import com.stock4event.backend.repository.EmpresaRepository;
import com.stock4event.backend.repository.EstoqueBarracaRepository;
import com.stock4event.backend.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class MarketplaceService {

    private static final List<String> TIPOS_FORNECEDOR = List.of("DISTRIBUIDOR", "AMBOS");
    private static final int LIMITE_MAXIMO = 60;

    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private BarracaRepository barracaRepository;
    @Autowired private EstoqueBarracaRepository estoqueBarracaRepository;

    public List<FornecedorResponseDTO> listarFornecedores(Long empresaCompradoraId) {
        Long excluirId = empresaCompradoraId != null ? empresaCompradoraId : -1L;
        return empresaRepository.findByTipoInAndIdNot(TIPOS_FORNECEDOR, excluirId).stream()
                .map(this::toFornecedorDTO)
                .toList();
    }

    public List<ProdutoResponseDTO> listarProdutosFornecedor(Long fornecedorId) {
        Empresa fornecedor = empresaRepository.findById(fornecedorId)
                .orElseThrow(() -> new CadastroException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado."));

        if (!TIPOS_FORNECEDOR.contains(fornecedor.getTipo())) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Empresa informada não é fornecedora.");
        }

        return produtoRepository.findByEmpresaIdAndAtivo(fornecedorId, 1).stream()
                .map(ProdutoResponseDTO::new)
                .toList();
    }

    /**
     * Produtos de todas as contas fornecedoras (DISTRIBUIDOR e AMBOS), dos cadastrados mais recentemente.
     * Os da própria empresa também aparecem, marcados como próprios (não dá para comprar de si mesmo).
     */
    @Transactional(readOnly = true)
    public List<ProdutoRecenteDTO> listarProdutosRecentes(Long empresaId, int limite) {
        int tamanho = Math.max(1, Math.min(limite, LIMITE_MAXIMO));
        return produtoRepository.findRecentesDeFornecedores(TIPOS_FORNECEDOR, PageRequest.of(0, tamanho))
                .stream()
                .map(p -> new ProdutoRecenteDTO(p, empresaId))
                .toList();
    }

    /**
     * Quiosques ativos de outras empresas (de clientes e de distribuidoras). A busca considera
     * o nome do quiosque, da empresa, do evento e dos produtos, sem diferenciar acentos.
     */
    @Transactional(readOnly = true)
    public List<QuiosqueDisponivelDTO> listarQuiosquesDisponiveis(Long empresaId, String busca) {
        String termo = normalizar(busca);
        List<QuiosqueDisponivelDTO> lista = new ArrayList<>();
        for (Barraca barraca : barracaRepository.findByAtivaAndEvento_Empresa_IdNotOrderByIdDesc(1, empresaId)) {
            List<EstoqueBarraca> estoque = estoqueBarracaRepository.findByBarracaId(barraca.getId()).stream()
                    .filter(e -> Integer.valueOf(1).equals(e.getProduto().getAtivo()))
                    .sorted(Comparator.comparing(e -> e.getProduto().getNome(), String.CASE_INSENSITIVE_ORDER))
                    .toList();
            Empresa dona = barraca.getEvento().getEmpresa();
            if (!termo.isEmpty() && !contem(termo, barraca.getNome(), dona.getNome(), barraca.getEvento().getNome())
                    && estoque.stream().noneMatch(e -> contem(termo, e.getProduto().getNome()))) {
                continue;
            }
            lista.add(new QuiosqueDisponivelDTO(barraca, estoque, TIPOS_FORNECEDOR.contains(dona.getTipo())));
        }
        return lista;
    }

    private static boolean contem(String termo, String... textos) {
        for (String t : textos) {
            if (normalizar(t).contains(termo)) return true;
        }
        return false;
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").trim().toLowerCase(Locale.ROOT);
    }

    private FornecedorResponseDTO toFornecedorDTO(Empresa empresa) {
        int total = produtoRepository.findByEmpresaIdAndAtivo(empresa.getId(), 1).size();
        return new FornecedorResponseDTO(empresa, total);
    }
}
