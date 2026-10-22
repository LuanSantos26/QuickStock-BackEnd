package com.stock4event.backend.service;

import com.stock4event.backend.dto.ItemVendaPdvRequestDTO;
import com.stock4event.backend.dto.ProdutoPdvDTO;
import com.stock4event.backend.dto.VendaPdvRequestDTO;
import com.stock4event.backend.dto.VendaPdvResponseDTO;
import com.stock4event.backend.entity.*;
import com.stock4event.backend.exception.CadastroException;
import com.stock4event.backend.repository.*;
import com.stock4event.backend.security.AcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.*;

/**
 * Vendas no PDV (ponto de venda = quiosque da empresa).
 * A venda vira um Pedido tipo "pdv" da barraca, como os pedidos de PDV já existentes, e por isso
 * entra no financeiro (vendas e formas de pagamento). Preços, totais, troco e estoque são
 * definidos aqui; nada disso é aceito do app.
 */
@Service
public class VendaPdvService {

    /** Mesmas formas aceitas no restante do sistema (SolicitacaoCompraService). */
    public static final List<String> FORMAS_PAGAMENTO = List.of("pix", "credito", "debito", "dinheiro");
    public static final String STATUS_CONCLUIDA = "concluido";

    @Autowired private BarracaRepository barracaRepository;
    @Autowired private EstoqueBarracaRepository estoqueBarracaRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ItemPedidoRepository itemPedidoRepository;
    @Autowired private PagamentoRepository pagamentoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    /** Produtos alocados no quiosque, filtrados por nome ou código (sem diferenciar acentos e maiúsculas). */
    @Transactional(readOnly = true)
    public List<ProdutoPdvDTO> buscarProdutos(Long barracaId, Long empresaId, String busca) {
        Barraca barraca = barracaDaEmpresa(barracaId, empresaId);
        String termo = normalizar(busca);
        return estoqueBarracaRepository.findByBarracaId(barraca.getId()).stream()
                .filter(e -> Integer.valueOf(1).equals(e.getProduto().getAtivo()))
                .filter(e -> termo.isEmpty()
                        || normalizar(e.getProduto().getNome()).contains(termo)
                        || normalizar(e.getProduto().getCodigo()).contains(termo))
                .map(ProdutoPdvDTO::new)
                .sorted(Comparator.comparing(ProdutoPdvDTO::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public VendaPdvResponseDTO buscarVenda(Long vendaId, Long empresaId) {
        Pedido pedido = pedidoRepository.findById(vendaId)
                .filter(p -> p.getChaveVenda() != null)
                .orElseThrow(() -> new CadastroException(HttpStatus.NOT_FOUND, "Venda não encontrada."));
        exigirVendaDaEmpresa(pedido, empresaId);
        return toResponse(pedido);
    }

    /** Venda já registrada com a chave informada (reenvio da mesma tentativa), se for da empresa. */
    @Transactional(readOnly = true)
    public Optional<VendaPdvResponseDTO> buscarPorChave(String chaveVenda, Long empresaId) {
        return pedidoRepository.findByChaveVenda(chaveVenda).map(p -> {
            exigirVendaDaEmpresa(p, empresaId);
            return toResponse(p);
        });
    }

    /**
     * Registra a venda numa única transação: pedido, itens, baixa de estoque (quiosque e produto)
     * e a forma de pagamento. Qualquer erro desfaz tudo. Linhas de estoque ficam bloqueadas até o
     * commit para que vendas simultâneas não deixem o estoque negativo.
     */
    @Transactional
    public VendaPdvResponseDTO finalizar(VendaPdvRequestDTO dto, Long empresaId, Long usuarioId) {
        String chave = dto.getChaveVenda().trim();
        Optional<VendaPdvResponseDTO> existente = buscarPorChave(chave, empresaId);
        if (existente.isPresent()) {
            return existente.get();
        }

        Barraca barraca = barracaDaEmpresa(dto.getBarracaId(), empresaId);
        if (!Integer.valueOf(1).equals(barraca.getAtiva())) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Este ponto de venda está desativado.");
        }
        String forma = dto.getFormaPagamento().trim().toLowerCase(Locale.ROOT);
        if (!FORMAS_PAGAMENTO.contains(forma)) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Forma de pagamento inválida.");
        }
        String clienteNome = textoOpcional(dto.getClienteNome());
        String clienteDocumento = documentoOpcional(dto.getClienteDocumento());
        Usuario operador = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new CadastroException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));

        // Mesmo produto repetido vira um item só; ordem fixa por id evita deadlock entre vendas simultâneas.
        Map<Long, BigDecimal> quantidades = new TreeMap<>();
        for (ItemVendaPdvRequestDTO item : dto.getItens()) {
            quantidades.merge(item.getProdutoId(), item.getQuantidade(), BigDecimal::add);
        }

        // Grava o pedido primeiro: a chave única barra uma segunda gravação da mesma venda.
        Pedido pedido = new Pedido();
        pedido.setTipo("pdv");
        pedido.setBarraca(barraca);
        pedido.setOperador(operador);
        pedido.setStatus(STATUS_CONCLUIDA);
        pedido.setMetodoPagamento(forma);
        pedido.setChaveVenda(chave);
        pedido.setClienteNome(clienteNome);
        pedido.setClienteDocumento(clienteDocumento);
        pedido.setValorTotal(BigDecimal.ZERO);
        pedido = pedidoRepository.saveAndFlush(pedido);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal> entrada : quantidades.entrySet()) {
            BigDecimal quantidade = entrada.getValue();
            Produto produto = produtoRepository.findByIdParaAtualizar(entrada.getKey())
                    .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Produto não encontrado: " + entrada.getKey()));
            if (!produto.getEmpresa().getId().equals(empresaId)) {
                throw AcessoService.proibido(AcessoService.EMPRESA_DIFERENTE);
            }
            if (!Integer.valueOf(1).equals(produto.getAtivo())) {
                throw new CadastroException(HttpStatus.BAD_REQUEST, "\"" + produto.getNome() + "\" não está mais à venda.");
            }
            EstoqueBarraca estoque = estoqueBarracaRepository.findParaAtualizar(barraca.getId(), produto.getId())
                    .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST,
                            "\"" + produto.getNome() + "\" não está disponível neste ponto de venda."));

            BigDecimal disponivel = ProdutoPdvDTO.disponivel(estoque, produto);
            if (quantidade.compareTo(disponivel) > 0) {
                throw new CadastroException(HttpStatus.BAD_REQUEST, "Estoque insuficiente para \"" + produto.getNome()
                        + "\". Disponível: " + disponivel.stripTrailingZeros().toPlainString());
            }

            estoque.setQuantidade(estoque.getQuantidade().subtract(quantidade));
            estoqueBarracaRepository.save(estoque);
            produto.setEstoque(produto.getEstoque().subtract(quantidade));
            produtoRepository.save(produto);

            BigDecimal preco = produto.getPrecoVenda();
            BigDecimal subtotal = preco.multiply(quantidade).setScale(2, RoundingMode.HALF_UP);
            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(preco);
            item.setSubtotal(subtotal);
            itemPedidoRepository.save(item);
            total = total.add(subtotal);
        }

        pedido.setValorTotal(total);
        if ("dinheiro".equals(forma)) {
            BigDecimal recebido = dto.getValorRecebido();
            if (recebido == null) {
                throw new CadastroException(HttpStatus.BAD_REQUEST, "Informe o valor recebido em dinheiro.");
            }
            if (recebido.compareTo(total) < 0) {
                throw new CadastroException(HttpStatus.BAD_REQUEST, "Valor recebido é menor que o total da venda.");
            }
            pedido.setValorRecebido(recebido.setScale(2, RoundingMode.HALF_UP));
            pedido.setTroco(recebido.subtract(total).setScale(2, RoundingMode.HALF_UP));
        }
        pedido = pedidoRepository.save(pedido);

        // Sem integração com operadora: só registra a forma de pagamento, sem aprovar cobrança.
        // (Venda de valor zero não tem o que registrar; o pagamento exige no mínimo R$ 0,01.)
        if (total.signum() > 0) {
            Pagamento pagamento = new Pagamento();
            pagamento.setPedido(pedido);
            pagamento.setMetodo(forma);
            pagamento.setValor(total);
            pagamento.setStatus("pendente");
            pagamento.setReferenciaPagamento("PDV");
            pagamentoRepository.save(pagamento);
        }

        return toResponse(pedido);
    }

    private VendaPdvResponseDTO toResponse(Pedido pedido) {
        return new VendaPdvResponseDTO(pedido, itemPedidoRepository.findByPedidoId(pedido.getId()));
    }

    private Barraca barracaDaEmpresa(Long barracaId, Long empresaId) {
        Barraca barraca = barracaRepository.findById(barracaId)
                .orElseThrow(() -> new CadastroException(HttpStatus.NOT_FOUND, "Ponto de venda não encontrado."));
        if (!AcessoService.empresaDaBarraca(barraca).equals(empresaId)) {
            throw AcessoService.proibido(AcessoService.EMPRESA_DIFERENTE);
        }
        return barraca;
    }

    private void exigirVendaDaEmpresa(Pedido pedido, Long empresaId) {
        if (pedido.getBarraca() == null || !AcessoService.empresaDaBarraca(pedido.getBarraca()).equals(empresaId)) {
            throw AcessoService.proibido(AcessoService.EMPRESA_DIFERENTE);
        }
    }

    private static String textoOpcional(String valor) {
        if (valor == null) return null;
        String limpo = valor.trim();
        return limpo.isEmpty() ? null : limpo;
    }

    /** CPF (11) ou CNPJ (14 dígitos), como no cadastro de conta. Vazio = cliente não identificado. */
    static String documentoOpcional(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String digitos = valor.replaceAll("\\D", "");
        if (digitos.length() != 11 && digitos.length() != 14) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Documento do cliente deve ser um CPF (11 dígitos) ou CNPJ (14 dígitos).");
        }
        return digitos;
    }

    static String normalizar(String texto) {
        if (texto == null) return "";
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.trim().toLowerCase(Locale.ROOT);
    }
}
