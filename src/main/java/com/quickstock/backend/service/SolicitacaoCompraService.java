package com.quickstock.backend.service;

import com.quickstock.backend.dto.EnderecoEntregaDTO;
import com.quickstock.backend.dto.SolicitacaoCompraRequestDTO;
import com.quickstock.backend.dto.SolicitacaoCompraResponseDTO;
import com.quickstock.backend.dto.StatusPedidoDTO;
import com.quickstock.backend.entity.*;
import com.quickstock.backend.exception.CadastroException;
import com.quickstock.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SolicitacaoCompraService {

    private static final List<String> TIPOS_FORNECEDOR = List.of("DISTRIBUIDOR", "PLATAFORMA");
    private static final List<String> METODOS_PAGAMENTO = List.of("pix", "credito", "debito", "dinheiro");
    private static final long SEGUNDOS_PARA_ROTA = 20L;

    @Autowired private SolicitacaoCompraRepository solicitacaoRepository;
    @Autowired private ItemSolicitacaoCompraRepository itemRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private EnderecoEntregaService enderecoEntregaService;

    public List<SolicitacaoCompraResponseDTO> listarPorComprador(Long empresaCompradoraId) {
        return solicitacaoRepository.findByEmpresaCompradoraIdOrderByCriadoEmDesc(empresaCompradoraId).stream()
                .map(s -> toResponse(atualizarStatusDemonstracao(s)))
                .toList();
    }

    public SolicitacaoCompraResponseDTO obterPorId(Long id, Long empresaCompradoraId) {
        SolicitacaoCompra solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new CadastroException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));

        if (!solicitacao.getEmpresaCompradora().getId().equals(empresaCompradoraId)) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Pedido não pertence à empresa informada.");
        }

        return toResponse(atualizarStatusDemonstracao(solicitacao));
    }

    @Transactional
    public SolicitacaoCompraResponseDTO criar(SolicitacaoCompraRequestDTO dto) {
        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Informe ao menos um item na solicitação.");
        }

        Empresa compradora = empresaRepository.findById(dto.getEmpresaCompradoraId())
                .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Empresa compradora não encontrada."));

        Empresa fornecedora = empresaRepository.findById(dto.getEmpresaFornecedoraId())
                .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Fornecedor não encontrado."));

        if (!TIPOS_FORNECEDOR.contains(fornecedora.getTipo())) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Empresa informada não é fornecedora.");
        }

        if (compradora.getId().equals(fornecedora.getId())) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Não é possível solicitar compra da própria empresa.");
        }

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Usuário não encontrado."));

        if (!usuario.getEmpresa().getId().equals(compradora.getId())) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Usuário não pertence à empresa compradora.");
        }

        String metodoPagamento = dto.getMetodoPagamento() != null
                ? dto.getMetodoPagamento().trim().toLowerCase()
                : "";
        if (!METODOS_PAGAMENTO.contains(metodoPagamento)) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Forma de pagamento inválida.");
        }

        if (dto.getEnderecoEntregaId() == null) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Informe o endereço de entrega.");
        }

        EnderecoEntrega endereco = enderecoEntregaService.validarEnderecoDaEmpresa(
                dto.getEnderecoEntregaId(), compradora.getId());

        BigDecimal taxaEntrega = dto.getTaxaEntrega() != null ? dto.getTaxaEntrega() : BigDecimal.ZERO;

        SolicitacaoCompra solicitacao = new SolicitacaoCompra();
        solicitacao.setEmpresaCompradora(compradora);
        solicitacao.setEmpresaFornecedora(fornecedora);
        solicitacao.setUsuarioSolicitante(usuario);
        solicitacao.setObservacao(dto.getObservacao());
        solicitacao.setMetodoPagamento(metodoPagamento);
        solicitacao.setEnderecoResumo(EnderecoEntregaDTO.formatarResumo(endereco));
        solicitacao.setCep(endereco.getCep());
        solicitacao.setLogradouro(endereco.getLogradouro());
        solicitacao.setNumero(endereco.getNumero());
        solicitacao.setComplemento(endereco.getComplemento());
        solicitacao.setBairro(endereco.getBairro());
        solicitacao.setCidade(endereco.getCidade());
        solicitacao.setUf(endereco.getUf());
        solicitacao.setTaxaEntrega(taxaEntrega);
        solicitacao.setStatus("aguardando_liberacao");
        solicitacao.setValorTotal(BigDecimal.ZERO);
        solicitacao = solicitacaoRepository.save(solicitacao);

        BigDecimal total = BigDecimal.ZERO;

        for (var itemDto : dto.getItens()) {
            Produto produto = produtoRepository.findById(itemDto.getProdutoId())
                    .orElseThrow(() -> new CadastroException(
                            HttpStatus.BAD_REQUEST,
                            "Produto não encontrado: " + itemDto.getProdutoId()
                    ));

            if (!produto.getEmpresa().getId().equals(fornecedora.getId())) {
                throw new CadastroException(HttpStatus.BAD_REQUEST, "Produto não pertence ao fornecedor selecionado.");
            }

            BigDecimal subtotal = produto.getPrecoVenda().multiply(itemDto.getQuantidade());

            ItemSolicitacaoCompra item = new ItemSolicitacaoCompra();
            item.setSolicitacao(solicitacao);
            item.setProduto(produto);
            item.setQuantidade(itemDto.getQuantidade());
            item.setPrecoUnitario(produto.getPrecoVenda());
            item.setSubtotal(subtotal);
            itemRepository.save(item);

            total = total.add(subtotal);
        }

        solicitacao.setValorTotal(total.add(taxaEntrega));
        solicitacao = solicitacaoRepository.save(solicitacao);

        return toResponse(solicitacao);
    }

    private SolicitacaoCompra atualizarStatusDemonstracao(SolicitacaoCompra solicitacao) {
        if (solicitacao.getCriadoEm() == null) return solicitacao;

        long segundos = Duration.between(solicitacao.getCriadoEm(), LocalDateTime.now()).getSeconds();
        String status = solicitacao.getStatus() != null ? solicitacao.getStatus() : "aguardando_liberacao";

        if ("aguardando_liberacao".equals(status) && segundos >= SEGUNDOS_PARA_ROTA) {
            solicitacao.setStatus("em_rota");
            return solicitacaoRepository.save(solicitacao);
        }

        return solicitacao;
    }

    private SolicitacaoCompraResponseDTO toResponse(SolicitacaoCompra solicitacao) {
        List<ItemSolicitacaoCompra> itens = itemRepository.findBySolicitacaoId(solicitacao.getId());
        String status = solicitacao.getStatus() != null ? solicitacao.getStatus() : "aguardando_liberacao";
        return new SolicitacaoCompraResponseDTO(
                solicitacao,
                itens,
                labelStatus(status),
                montarEtapas(status)
        );
    }

    private String labelStatus(String status) {
        return switch (status) {
            case "aguardando_liberacao" -> "Aguardando liberação da distribuidora";
            case "em_rota" -> "Saindo para rota de entrega";
            case "entregue" -> "Pedido entregue";
            case "cancelada" -> "Pedido cancelado";
            default -> "Pedido em andamento";
        };
    }

    private List<StatusPedidoDTO> montarEtapas(String status) {
        int indiceAtual = switch (status) {
            case "aguardando_liberacao" -> 1;
            case "em_rota" -> 2;
            case "entregue" -> 3;
            default -> 1;
        };

        List<StatusPedidoDTO> etapas = new ArrayList<>();
        etapas.add(new StatusPedidoDTO(
                "pedido_efetuado",
                "Pedido efetuado com sucesso",
                1,
                true,
                indiceAtual == 0
        ));
        etapas.add(new StatusPedidoDTO(
                "aguardando_liberacao",
                "Aguardando liberação da distribuidora",
                2,
                indiceAtual >= 2,
                indiceAtual == 1
        ));
        etapas.add(new StatusPedidoDTO(
                "em_rota",
                "Saindo para rota de entrega",
                3,
                indiceAtual >= 3,
                indiceAtual == 2
        ));
        return etapas;
    }
}
