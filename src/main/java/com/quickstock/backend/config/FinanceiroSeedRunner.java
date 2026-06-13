package com.quickstock.backend.config;

import com.quickstock.backend.entity.*;
import com.quickstock.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Component
@Order(100)
public class FinanceiroSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FinanceiroSeedRunner.class);
    private static final String OBS_SEED = "SEED_FINANCEIRO_DEMO";

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ProdutoRepository produtoRepository;
    private final SolicitacaoCompraRepository solicitacaoRepository;
    private final ItemSolicitacaoCompraRepository itemRepository;
    private final JdbcTemplate jdbcTemplate;

    public FinanceiroSeedRunner(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            ProdutoRepository produtoRepository,
            SolicitacaoCompraRepository solicitacaoRepository,
            ItemSolicitacaoCompraRepository itemRepository,
            JdbcTemplate jdbcTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.produtoRepository = produtoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.itemRepository = itemRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Empresa> fornecedoras = empresaRepository.findByTipoInAndIdNot(
                List.of("DISTRIBUIDOR", "PLATAFORMA"), -1L);
        if (fornecedoras.isEmpty()) {
            log.info("Financeiro seed: nenhuma fornecedora disponível.");
            return;
        }

        int criadas = 0;
        List<Usuario> usuarios = usuarioRepository.findAll();

        for (Usuario usuario : usuarios) {
            Empresa compradora = usuario.getEmpresa();
            if (compradora == null) continue;

            long existentes = solicitacaoRepository.findByEmpresaCompradoraIdOrderByCriadoEmDesc(compradora.getId())
                    .stream()
                    .filter(s -> OBS_SEED.equals(s.getObservacao()))
                    .count();
            if (existentes >= 12) continue;

            criadas += seedParaEmpresa(compradora, usuario, fornecedoras);
        }

        if (criadas > 0) {
            log.info("Financeiro seed: {} solicitações demo criadas.", criadas);
        }
    }

    private int seedParaEmpresa(Empresa compradora, Usuario usuario, List<Empresa> fornecedoras) {
        int count = 0;
        YearMonth atual = YearMonth.now();

        for (int m = 5; m >= 0; m--) {
            YearMonth mes = atual.minusMonths(m);
            int pedidosNoMes = 3 + (m % 2);

            for (int p = 0; p < pedidosNoMes; p++) {
                Empresa fornecedora = fornecedoras.get((m + p) % fornecedoras.size());
                List<Produto> produtos = produtoRepository.findByEmpresaIdAndAtivo(fornecedora.getId(), 1);
                if (produtos.isEmpty()) continue;

                Produto produto = produtos.get(p % produtos.size());
                BigDecimal qtd = BigDecimal.valueOf(10 + p * 5);
                BigDecimal subtotal = produto.getPrecoVenda().multiply(qtd);

                SolicitacaoCompra solicitacao = new SolicitacaoCompra();
                solicitacao.setEmpresaCompradora(compradora);
                solicitacao.setEmpresaFornecedora(fornecedora);
                solicitacao.setUsuarioSolicitante(usuario);
                solicitacao.setObservacao(OBS_SEED);
                solicitacao.setStatus("enviada");
                solicitacao.setValorTotal(subtotal);
                solicitacao = solicitacaoRepository.save(solicitacao);

                ItemSolicitacaoCompra item = new ItemSolicitacaoCompra();
                item.setSolicitacao(solicitacao);
                item.setProduto(produto);
                item.setQuantidade(qtd);
                item.setPrecoUnitario(produto.getPrecoVenda());
                item.setSubtotal(subtotal);
                itemRepository.save(item);

                LocalDateTime data = mes.atDay(5 + p * 4).atTime(10 + p, 30);
                jdbcTemplate.update(
                        "UPDATE solicitacoes_compra SET criado_em = ? WHERE id = ?",
                        data, solicitacao.getId());

                count++;
            }
        }
        return count;
    }
}
