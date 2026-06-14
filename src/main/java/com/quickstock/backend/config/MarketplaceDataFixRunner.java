package com.quickstock.backend.config;

import com.quickstock.backend.entity.Empresa;
import com.quickstock.backend.entity.Produto;
import com.quickstock.backend.repository.EmpresaRepository;
import com.quickstock.backend.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class MarketplaceDataFixRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceDataFixRunner.class);

    private final EmpresaRepository empresaRepository;
    private final ProdutoRepository produtoRepository;

    public MarketplaceDataFixRunner(
            EmpresaRepository empresaRepository,
            ProdutoRepository produtoRepository) {
        this.empresaRepository = empresaRepository;
        this.produtoRepository = produtoRepository;
    }

    private record EmpresaFix(String nome, String descricao, String logoUrl, String capaUrl) {}

    private record ProdutoFix(String nome, BigDecimal preco, String descricao, String imagemUrl, int estoqueInicial) {}

    private static String logo(String initials, String bg) {
        return "https://ui-avatars.com/api/?name=" + initials
                + "&background=" + bg + "&color=fff&bold=true&size=200&format=png";
    }

    private static String capa(String seed) {
        return "https://picsum.photos/seed/" + seed + "/800/400";
    }

    private static String img(String seed) {
        return "https://picsum.photos/seed/" + seed + "/400/400";
    }

    private static final Map<String, EmpresaFix> EMPRESAS = Map.of(
            "30.001.001/0001-01", new EmpresaFix(
                    "Casa dos Vinhos",
                    "Vinhos nacionais e importados selecionados para bares, restaurantes e revenda.",
                    logo("CV", "722F37"),
                    capa("casa-dos-vinhos")),
            "30.002.002/0001-02", new EmpresaFix(
                    "Cervejaria Caruaru",
                    "Cervejas geladas e importadas para revenda em Caruaru e região.",
                    logo("CC", "F8B125"),
                    capa("cervejaria-caruaru")),
            "30.003.003/0001-03", new EmpresaFix(
                    "Whisky Labs",
                    "Whiskies, conhaques e destilados premium para colecionadores e revenda.",
                    logo("WL", "1a365d"),
                    capa("whisky-labs"))
    );

    private static final Map<String, List<ProdutoFix>> PRODUTOS_POR_CNPJ = Map.of(
            "30.001.001/0001-01", List.of(
                    new ProdutoFix("Vinho Mazzei Zisola Sicilia Noto Rosso 750ml", bd("156.00"),
                            "Vinho tinto siciliano Noto Rosso, garrafa 750 ml.", img("vinho-mazzei"), 150),
                    new ProdutoFix("Vinho Louis Latour Bourgogne Chardonnay 750 ml", bd("200.00"),
                            "Vinho branco Bourgogne Chardonnay, garrafa 750 ml.", img("vinho-latour"), 100),
                    new ProdutoFix("Vinho Naturelle Tinto Reserva 750 ML", bd("60.00"),
                            "Vinho tinto reserva Naturelle, garrafa 750 ml.", img("vinho-naturelle"), 300),
                    new ProdutoFix("Vinho Carmin De Peumo Carmenere 750 ml", bd("666.00"),
                            "Vinho tinto Carmenere Carmin De Peumo, garrafa 750 ml.", img("vinho-carmin"), 80),
                    new ProdutoFix("Vinho Carolina Reserva Sauvignon Blanc 750 ml", bd("60.00"),
                            "Vinho branco Sauvignon Blanc reserva Carolina, garrafa 750 ml.", img("vinho-carolina"), 200)
            ),
            "30.002.002/0001-02", List.of(
                    new ProdutoFix("Cerveja Heineken Long Neck 330ml", bd("5.99"),
                            "Cerveja Heineken long neck 330 ml.", img("cerveja-heineken"), 200),
                    new ProdutoFix("Cerveja Corona Long Neck", bd("6.99"),
                            "Cerveja Corona Extra long neck 330 ml.", img("cerveja-corona"), 150),
                    new ProdutoFix("Cerveja Amstel Lata 269ml", bd("3.19"),
                            "Cerveja Amstel lata 269 ml.", img("cerveja-amstel"), 300),
                    new ProdutoFix("Cerveja Cerpa Export Long Neck 350ml", bd("8.39"),
                            "Cerveja Cerpa Export long neck 350 ml.", img("cerveja-cerpa"), 100),
                    new ProdutoFix("Cerveja Paulaner Munchen Weissbier 500ml", bd("11.19"),
                            "Cerveja Paulaner Munchen Weissbier garrafa 500 ml.", img("cerveja-paulaner"), 80)
            ),
            "30.003.003/0001-03", List.of(
                    new ProdutoFix("Whisky Escocês Royal Salute 21 Anos The Signature Blend 700ml", bd("989.00"),
                            "Whisky escocês Royal Salute 21 anos, garrafa 700 ml.", img("whisky-royal-salute"), 150),
                    new ProdutoFix("Whisky The Balvenie PortWood 21 Anos 700ml Single Malt Escocês", bd("2795.30"),
                            "Whisky single malt The Balvenie PortWood 21 anos, garrafa 700 ml.", img("whisky-balvenie"), 100),
                    new ProdutoFix("Whisky Glenfiddich Single Malt 12 Anos 750ml Escocês", bd("415.75"),
                            "Whisky single malt Glenfiddich 12 anos, garrafa 750 ml.", img("whisky-glenfiddich"), 300),
                    new ProdutoFix("Conhaque Martell L'Or de Jean Martell 700ml", bd("18711.00"),
                            "Conhaque Martell L'Or de Jean Martell, garrafa 700 ml.", img("conhaque-martell"), 80),
                    new ProdutoFix("Whisky Suntory Hibiki Japanese Harmony 700ml | Blended Premium Japonês", bd("729.90"),
                            "Whisky japonês Suntory Hibiki Japanese Harmony, garrafa 700 ml.", img("whisky-hibiki"), 200)
            )
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int empresasAtualizadas = 0;
        int produtosAtualizados = 0;

        for (Map.Entry<String, EmpresaFix> entry : EMPRESAS.entrySet()) {
            Optional<Empresa> empresaOpt = empresaRepository.findByCnpj(entry.getKey());
            if (empresaOpt.isEmpty()) {
                log.warn("Marketplace seed: CNPJ {} não encontrado no banco.", entry.getKey());
                continue;
            }

            Empresa empresa = empresaOpt.get();
            aplicarFixEmpresa(empresa, entry.getValue());
            empresa.setTipo("DISTRIBUIDOR");
            empresaRepository.save(empresa);
            empresasAtualizadas++;

            List<ProdutoFix> produtos = PRODUTOS_POR_CNPJ.getOrDefault(entry.getKey(), List.of());
            for (ProdutoFix fix : produtos) {
                produtosAtualizados += sincronizarProduto(empresa, fix);
            }
        }

        log.info("Marketplace seed: {} empresas e {} produtos sincronizados.", empresasAtualizadas, produtosAtualizados);
    }

    private int sincronizarProduto(Empresa empresa, ProdutoFix fix) {
        Optional<Produto> existente = produtoRepository.findByEmpresaIdAndAtivo(empresa.getId(), 1).stream()
                .filter(p -> fix.nome().equals(p.getNome()))
                .findFirst();

        if (existente.isEmpty()) {
            existente = produtoRepository.findAll().stream()
                    .filter(p -> empresa.getId().equals(p.getEmpresa().getId()))
                    .filter(p -> fix.nome().equals(p.getNome()))
                    .findFirst();
        }

        Produto produto = existente.orElseGet(() -> {
            Produto novo = new Produto();
            novo.setEmpresa(empresa);
            novo.setNome(fix.nome());
            novo.setUnidade("UN");
            novo.setAtivo(1);
            novo.setEstoque(BigDecimal.valueOf(fix.estoqueInicial()));
            return novo;
        });

        produto.setNome(fix.nome());
        produto.setDescricao(fix.descricao());
        produto.setPrecoVenda(fix.preco());
        produto.setImagemUrl(fix.imagemUrl());
        produto.setAtivo(1);
        if (produto.getEstoque() == null) {
            produto.setEstoque(BigDecimal.valueOf(fix.estoqueInicial()));
        }
        produtoRepository.save(produto);
        return 1;
    }

    private void aplicarFixEmpresa(Empresa empresa, EmpresaFix fix) {
        empresa.setNome(fix.nome());
        empresa.setDescricao(fix.descricao());
        empresa.setLogoUrl(fix.logoUrl());
        empresa.setCapaUrl(fix.capaUrl());
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
