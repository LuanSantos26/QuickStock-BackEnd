package com.quickstock.backend.service;

import com.quickstock.backend.dto.*;
import com.quickstock.backend.entity.SolicitacaoCompra;
import com.quickstock.backend.exception.CadastroException;
import com.quickstock.backend.repository.EmpresaRepository;
import com.quickstock.backend.repository.SolicitacaoCompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinanceiroService {

    private static final DateTimeFormatter MES_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final String[] LABELS_PT = {
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    };

    /** Lucro base mensal (bebidas / atacado). */
    private static final BigDecimal[] LUCROS_BASE = {
            bd("1980"), bd("2150"), bd("2320"), bd("2080"), bd("2450"), bd("2180")
    };

    /** Gastos base mensal. */
    private static final BigDecimal[] GASTOS_BASE = {
            bd("3100"), bd("2950"), bd("3280"), bd("3020"), bd("3180"), bd("3050")
    };

    /** Compras B2B base mensal. */
    private static final BigDecimal[] COMPRAS_BASE = {
            bd("4800"), bd("5200"), bd("4950"), bd("5500"), bd("5100"), bd("5400")
    };

    /** Pedidos (vendas barraca) base mensal. */
    private static final int[] PEDIDOS_BASE = { 14, 16, 18, 15, 20, 17 };

    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private SolicitacaoCompraRepository solicitacaoRepository;

    public FinanceiroResumoDTO obterResumo(Long empresaCompradoraId) {
        empresaRepository.findById(empresaCompradoraId)
                .orElseThrow(() -> new CadastroException(HttpStatus.NOT_FOUND, "Empresa não encontrada."));

        int variacao = (int) (empresaCompradoraId % 5);
        List<YearMonth> meses = ultimosMeses(6);

        Map<YearMonth, BigDecimal> comprasReais = agruparComprasPorMes(empresaCompradoraId, meses);

        List<MesLucroDTO> lucrosMensais = new ArrayList<>();
        List<MesValorDTO> comprasMensais = new ArrayList<>();
        List<MesPedidosDTO> pedidosMensais = new ArrayList<>();

        BigDecimal somaLucro = BigDecimal.ZERO;
        BigDecimal somaCompras = BigDecimal.ZERO;
        int somaPedidos = 0;

        for (int i = 0; i < meses.size(); i++) {
            YearMonth ym = meses.get(i);
            String mes = ym.format(MES_FMT);
            String label = LABELS_PT[ym.getMonthValue() - 1];

            BigDecimal lucro = LUCROS_BASE[i].add(bd(String.valueOf(variacao * 45 + i * 12)));
            BigDecimal gastos = GASTOS_BASE[i].add(bd(String.valueOf(variacao * 30)));
            lucrosMensais.add(new MesLucroDTO(mes, label, lucro, gastos));
            somaLucro = somaLucro.add(lucro);

            BigDecimal compra = comprasReais.getOrDefault(ym,
                    COMPRAS_BASE[i].add(bd(String.valueOf(variacao * 80 + i * 25))));
            comprasMensais.add(new MesValorDTO(mes, label, compra));
            somaCompras = somaCompras.add(compra);

            int pedidos = PEDIDOS_BASE[i] + variacao + (i % 2);
            pedidosMensais.add(new MesPedidosDTO(mes, label, pedidos));
            somaPedidos += pedidos;
        }

        BigDecimal lucroMesAtual = lucrosMensais.get(meses.size() - 1).getLucro();
        BigDecimal totalComprasMesAtual = comprasMensais.get(meses.size() - 1).getValor();
        int totalPedidosMesAtual = pedidosMensais.get(meses.size() - 1).getQuantidade();

        BigDecimal mediaLucro = somaLucro.divide(bd(String.valueOf(meses.size())), 2, RoundingMode.HALF_UP);
        BigDecimal mediaCompras = somaCompras.divide(bd(String.valueOf(meses.size())), 2, RoundingMode.HALF_UP);
        int mediaPedidos = Math.round((float) somaPedidos / meses.size());

        BigDecimal lucroTotal = somaLucro.multiply(bd("1.85")).setScale(2, RoundingMode.HALF_UP);
        int margem = lucroMesAtual.multiply(bd("100"))
                .divide(lucroMesAtual.add(GASTOS_BASE[meses.size() - 1]), 0, RoundingMode.HALF_UP)
                .intValue();

        List<FormaPagamentoDTO> formasPagamento = montarFormasPagamento(lucroTotal, variacao);

        return new FinanceiroResumoDTO(
                lucroTotal,
                lucroMesAtual,
                mediaLucro,
                Math.min(65, Math.max(28, margem)),
                mediaPedidos,
                totalPedidosMesAtual,
                totalComprasMesAtual,
                mediaCompras,
                lucrosMensais,
                comprasMensais,
                pedidosMensais,
                formasPagamento
        );
    }

    private Map<YearMonth, BigDecimal> agruparComprasPorMes(Long empresaId, List<YearMonth> meses) {
        YearMonth inicio = meses.get(0);
        LocalDateTime desde = inicio.atDay(1).atStartOfDay();

        return solicitacaoRepository.findByEmpresaCompradoraIdOrderByCriadoEmDesc(empresaId).stream()
                .filter(s -> s.getCriadoEm() != null && !s.getCriadoEm().isBefore(desde))
                .collect(Collectors.groupingBy(
                        s -> YearMonth.from(s.getCriadoEm()),
                        Collectors.reducing(BigDecimal.ZERO, SolicitacaoCompra::getValorTotal, BigDecimal::add)
                ));
    }

    private List<FormaPagamentoDTO> montarFormasPagamento(BigDecimal base, int variacao) {
        int pixPct = 42 + variacao;
        int creditoPct = 28 - (variacao / 2);
        int debitoPct = 18;
        int dinheiroPct = 100 - pixPct - creditoPct - debitoPct;

        BigDecimal totalVendas = base.multiply(bd("2.4"));
        return List.of(
                new FormaPagamentoDTO("pix", "PIX",
                        pixPct, percentualDe(totalVendas, pixPct)),
                new FormaPagamentoDTO("credito", "Crédito",
                        creditoPct, percentualDe(totalVendas, creditoPct)),
                new FormaPagamentoDTO("debito", "Débito",
                        debitoPct, percentualDe(totalVendas, debitoPct)),
                new FormaPagamentoDTO("dinheiro", "Dinheiro",
                        dinheiroPct, percentualDe(totalVendas, dinheiroPct))
        );
    }

    private BigDecimal percentualDe(BigDecimal total, int pct) {
        return total.multiply(bd(String.valueOf(pct)))
                .divide(bd("100"), 2, RoundingMode.HALF_UP);
    }

    private List<YearMonth> ultimosMeses(int qtd) {
        YearMonth atual = YearMonth.now();
        List<YearMonth> lista = new ArrayList<>();
        for (int i = qtd - 1; i >= 0; i--) {
            lista.add(atual.minusMonths(i));
        }
        return lista;
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
