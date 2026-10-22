package com.stock4event.backend.config;

import com.stock4event.backend.entity.Empresa;
import com.stock4event.backend.entity.Produto;
import com.stock4event.backend.repository.EmpresaRepository;
import com.stock4event.backend.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    private record ProdutoFix(String codigo, String nome, BigDecimal preco, String descricao, String imagemUrl, int estoqueInicial) {}

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

    private static final String LOGO_CASA_DOS_VINHOS = "/uploads/empresas/casa-dos-vinhos-logo.png";
    private static final String CAPA_CASA_DOS_VINHOS = "/uploads/empresas/casa-dos-vinhos-capa.png";
    private static final String LOGO_WHISKY_LABS = "/uploads/empresas/whisky-labs-logo.png";
    private static final String CAPA_WHISKY_LABS = "/uploads/empresas/whisky-labs-capa.png";
    private static final String LOGO_CERVEJARIA_CARUARU = "/uploads/empresas/cervejaria-caruaru-logo.png";
    private static final String CAPA_CERVEJARIA_CARUARU = "/uploads/empresas/cervejaria-caruaru-capa.png";
    private static final String IMG_CERVEJA_HEINEKEN = "/uploads/produtos/cerveja-heineken-long-neck.png";
    private static final String IMG_CERVEJA_CORONA = "/uploads/produtos/cerveja-corona-long-neck.png";
    private static final String IMG_CERVEJA_AMSTEL = "/uploads/produtos/cerveja-amstel-lata.png";
    private static final String IMG_CERVEJA_CERPA = "/uploads/produtos/cerveja-cerpa-export-long-neck.png";
    private static final String IMG_CERVEJA_PAULANER = "/uploads/produtos/cerveja-paulaner-munchen-weissbier.png";
    private static final String IMG_CONHAQUE_MARTELL = "/uploads/produtos/conhaque-martell-lor.png";
    private static final String IMG_WHISKY_ROYAL_SALUTE = "/uploads/produtos/whisky-royal-salute-21.png";
    private static final String IMG_WHISKY_BALVENIE = "/uploads/produtos/whisky-balvenie-portwood-21.png";
    private static final String IMG_WHISKY_GLENFIDDICH = "/uploads/produtos/whisky-glenfiddich-12.png";
    private static final String IMG_WHISKY_HIBIKI = "/uploads/produtos/whisky-hibiki-japanese-harmony.png";

    private static final Map<String, EmpresaFix> EMPRESAS = Map.of(
            "30.001.001/0001-01", new EmpresaFix(
                    "Casa dos Vinhos",
                    "Vinhos, espumantes e sucos de uva sem álcool, nacionais e importados, para bares, restaurantes e eventos.",
                    LOGO_CASA_DOS_VINHOS,
                    CAPA_CASA_DOS_VINHOS),
            "30.002.002/0001-02", new EmpresaFix(
                    "Cervejaria Caruaru",
                    "Cervejas geladas e importadas para revenda em Caruaru e região.",
                    LOGO_CERVEJARIA_CARUARU,
                    CAPA_CERVEJARIA_CARUARU),
            "30.003.003/0001-03", new EmpresaFix(
                    "Whisky Labs",
                    "Especialistas na distribuição de whiskies, conhaques e destilados premium para colecionadores e revendedores.",
                    LOGO_WHISKY_LABS,
                    CAPA_WHISKY_LABS),
            "30.004.004/0001-04", new EmpresaFix(
                    "SaborViva",
                    "Águas, sucos e refrigerantes para bares, mercados e eventos. Bebidas para hoje, negócios para sempre.",
                    "/uploads/empresas/saborviva-logo.png",
                    "/uploads/empresas/saborviva-capa.png")
    );

    private static final Map<String, List<ProdutoFix>> PRODUTOS_POR_CNPJ = Map.of(
            "30.001.001/0001-01", List.of(
                    new ProdutoFix("MKT-CDV-006", "Suco de uva integral Serra Gaúcha 1 L", bd("17.90"),
                            "Linha Acessível · origem Brasil. Bebida sem álcool.", "/uploads/produtos/cdv-suco-de-uva-integral-serra-gaucha-1-l.png", 120),
                    new ProdutoFix("MKT-CDV-007", "Suco de uva branca integral 1 L", bd("19.90"),
                            "Linha Acessível · origem Brasil. Bebida sem álcool.", "/uploads/produtos/cdv-suco-de-uva-branca-integral-1-l.png", 90),
                    new ProdutoFix("MKT-CDV-008", "Néctar de uva premium 1 L", bd("14.90"),
                            "Linha Acessível · origem Brasil. Bebida sem álcool.", "/uploads/produtos/cdv-nectar-de-uva-premium-1-l.png", 150),
                    new ProdutoFix("MKT-CDV-009", "Espumante de uva sem álcool 660 ml", bd("24.90"),
                            "Linha Acessível · origem Brasil. Bebida sem álcool.", "/uploads/produtos/cdv-espumante-de-uva-sem-alcool-660-ml.png", 80),
                    new ProdutoFix("MKT-CDV-010", "Suco de uva rosé integral 1 L", bd("22.90"),
                            "Linha Acessível · origem Brasil. Bebida sem álcool.", "/uploads/produtos/cdv-suco-de-uva-rose-integral-1-l.png", 70),
                    new ProdutoFix("MKT-CDV-011", "Vinho tinto desalcoolizado 750 ml", bd("49.90"),
                            "Linha Intermediário · origem Brasil. Bebida sem álcool.", "/uploads/produtos/cdv-vinho-tinto-desalcoolizado-750-ml.png", 65),
                    new ProdutoFix("MKT-CDV-012", "Vinho branco desalcoolizado 750 ml", bd("54.90"),
                            "Linha Intermediário · origem Brasil. Bebida sem álcool.", "/uploads/produtos/cdv-vinho-branco-desalcoolizado-750-ml.png", 55),
                    new ProdutoFix("MKT-CDV-013", "Espumante brut sem álcool 750 ml", bd("59.90"),
                            "Linha Intermediário · origem Brasil. Bebida sem álcool.", "/uploads/produtos/cdv-espumante-brut-sem-alcool-750-ml.png", 60),
                    new ProdutoFix("MKT-CDV-014", "Suco de uva orgânico reserva 750 ml", bd("39.90"),
                            "Linha Intermediário · origem Brasil. Bebida sem álcool.", "/uploads/produtos/cdv-suco-de-uva-organico-reserva-750-ml.png", 75),
                    new ProdutoFix("MKT-CDV-015", "Vinho rosé desalcoolizado 750 ml", bd("79.90"),
                            "Linha Intermediário · origem Espanha. Bebida sem álcool.", "/uploads/produtos/cdv-vinho-rose-desalcoolizado-750-ml.png", 40),
                    new ProdutoFix("MKT-CDV-016", "Espumante rosé sem álcool 750 ml", bd("119.90"),
                            "Linha Premium · origem França. Bebida sem álcool.", "/uploads/produtos/cdv-espumante-rose-sem-alcool-750-ml.png", 30),
                    new ProdutoFix("MKT-CDV-017", "Vinho tinto desalcoolizado reserva 750 ml", bd("129.90"),
                            "Linha Premium · origem Espanha. Bebida sem álcool.", "/uploads/produtos/cdv-vinho-tinto-desalcoolizado-reserva-750-ml.png", 25),
                    new ProdutoFix("MKT-CDV-018", "Vinho branco desalcoolizado Chardonnay 750 ml", bd("139.90"),
                            "Linha Premium · origem Alemanha. Bebida sem álcool.", "/uploads/produtos/cdv-vinho-branco-desalcoolizado-chardonnay-750-ml.png", 22),
                    new ProdutoFix("MKT-CDV-019", "Espumante sem álcool cuvée 750 ml", bd("149.90"),
                            "Linha Premium · origem Itália. Bebida sem álcool.", "/uploads/produtos/cdv-espumante-sem-alcool-cuvee-750-ml.png", 18),
                    new ProdutoFix("MKT-CDV-020", "Vinho tinto desalcoolizado premium 750 ml", bd("169.90"),
                            "Linha Premium · origem França. Bebida sem álcool.", "/uploads/produtos/cdv-vinho-tinto-desalcoolizado-premium-750-ml.png", 15)
            ),
            "30.002.002/0001-02", List.of(
                    new ProdutoFix("MKT-CC-001", "Cerveja Heineken Long Neck 330ml", bd("5.99"),
                            "Cerveja Heineken long neck 330 ml.", IMG_CERVEJA_HEINEKEN, 200),
                    new ProdutoFix("MKT-CC-002", "Cerveja Corona Long Neck", bd("6.99"),
                            "Cerveja Corona Extra long neck 330 ml.", IMG_CERVEJA_CORONA, 150),
                    new ProdutoFix("MKT-CC-003", "Cerveja Amstel Lata 269ml", bd("3.19"),
                            "Cerveja Amstel lata 269 ml.", IMG_CERVEJA_AMSTEL, 300),
                    new ProdutoFix("MKT-CC-004", "Cerveja Cerpa Export Long Neck 350ml", bd("8.39"),
                            "Cerveja Cerpa Export long neck 350 ml.", IMG_CERVEJA_CERPA, 100),
                    new ProdutoFix("MKT-CC-005", "Cerveja Paulaner Munchen Weissbier 500ml", bd("11.19"),
                            "Cerveja Paulaner Munchen Weissbier garrafa 500 ml.", IMG_CERVEJA_PAULANER, 80)
            ),
            "30.003.003/0001-03", List.of(
                    new ProdutoFix("MKT-WL-001", "Whisky Escocês Royal Salute 21 Anos The Signature Blend 700ml", bd("989.00"),
                            "Whisky escocês Royal Salute 21 anos, garrafa 700 ml.", IMG_WHISKY_ROYAL_SALUTE, 150),
                    new ProdutoFix("MKT-WL-002", "Whisky The Balvenie PortWood 21 Anos 700ml Single Malt Escocês", bd("2795.30"),
                            "Whisky single malt The Balvenie PortWood 21 anos, garrafa 700 ml.", IMG_WHISKY_BALVENIE, 100),
                    new ProdutoFix("MKT-WL-003", "Whisky Glenfiddich Single Malt 12 Anos 750ml Escocês", bd("415.75"),
                            "Whisky single malt Glenfiddich 12 anos, garrafa 750 ml.", IMG_WHISKY_GLENFIDDICH, 300),
                    new ProdutoFix("MKT-WL-004", "Conhaque Martell L'Or de Jean Martell 700ml", bd("18711.00"),
                            "Conhaque Martell L'Or de Jean Martell, garrafa 700 ml.", IMG_CONHAQUE_MARTELL, 80),
                    new ProdutoFix("MKT-WL-005", "Whisky Suntory Hibiki Japanese Harmony 700ml | Blended Premium Japonês", bd("729.90"),
                            "Whisky japonês Suntory Hibiki Japanese Harmony, garrafa 700 ml.", IMG_WHISKY_HIBIKI, 200)
            ),
            "30.004.004/0001-04", List.of(
                    new ProdutoFix("MKT-SV-001", "Água mineral sem gás 500 ml", bd("2.50"),
                            "Água mineral natural sem gás, garrafa 500 ml.", "/uploads/produtos/saborviva-agua-sem-gas-500ml.png", 500),
                    new ProdutoFix("MKT-SV-002", "Água mineral com gás 500 ml", bd("3.50"),
                            "Água mineral com gás, garrafa 500 ml.", "/uploads/produtos/saborviva-agua-com-gas-500ml.png", 350),
                    new ProdutoFix("MKT-SV-003", "Água mineral sem gás 1,5 L", bd("5.00"),
                            "Água mineral natural sem gás, garrafa 1,5 L.", "/uploads/produtos/saborviva-agua-sem-gas-1-5l.png", 250),
                    new ProdutoFix("MKT-SV-004", "Água mineral galão 5 L", bd("12.00"),
                            "Água mineral natural sem gás, galão 5 L.", "/uploads/produtos/saborviva-agua-galao-5l.png", 100),
                    new ProdutoFix("MKT-SV-005", "Água de coco 330 ml", bd("6.50"),
                            "Água de coco natural, caixinha 330 ml.", "/uploads/produtos/saborviva-agua-de-coco-330ml.png", 180),
                    new ProdutoFix("MKT-SV-006", "Suco de laranja 1 L", bd("9.90"),
                            "Suco de laranja 100% fruta, caixa 1 L.", "/uploads/produtos/saborviva-suco-laranja-1l.png", 200),
                    new ProdutoFix("MKT-SV-007", "Suco de uva integral 1 L", bd("14.90"),
                            "Suco de uva integral 100% fruta, caixa 1 L.", "/uploads/produtos/saborviva-suco-uva-integral-1l.png", 150),
                    new ProdutoFix("MKT-SV-008", "Suco de maçã 1 L", bd("10.50"),
                            "Suco de maçã 100% fruta, caixa 1 L.", "/uploads/produtos/saborviva-suco-maca-1l.png", 180),
                    new ProdutoFix("MKT-SV-009", "Suco de manga 1 L", bd("8.90"),
                            "Suco de manga 100% fruta, caixa 1 L.", "/uploads/produtos/saborviva-suco-manga-1l.png", 220),
                    new ProdutoFix("MKT-SV-010", "Suco de maracujá 1 L", bd("9.50"),
                            "Suco de maracujá 100% fruta, caixa 1 L.", "/uploads/produtos/saborviva-suco-maracuja-1l.png", 180),
                    new ProdutoFix("MKT-SV-011", "Coca-Cola Original 2 L", bd("11.90"),
                            "Refrigerante Coca-Cola sabor original, garrafa 2 L.", "/uploads/produtos/saborviva-coca-cola-original-2l.png", 450),
                    new ProdutoFix("MKT-SV-012", "Coca-Cola Zero 2 L", bd("11.90"),
                            "Refrigerante Coca-Cola zero açúcar, garrafa 2 L.", "/uploads/produtos/saborviva-coca-cola-zero-2l.png", 300),
                    new ProdutoFix("MKT-SV-013", "Guaraná Antarctica 2 L", bd("9.50"),
                            "Refrigerante Guaraná Antarctica, garrafa 2 L.", "/uploads/produtos/saborviva-guarana-antarctica-2l.png", 500),
                    new ProdutoFix("MKT-SV-014", "Sprite 2 L", bd("9.90"),
                            "Refrigerante Sprite sabor limão, garrafa 2 L.", "/uploads/produtos/saborviva-sprite-2l.png", 400),
                    new ProdutoFix("MKT-SV-015", "Fanta Laranja 2 L", bd("9.50"),
                            "Refrigerante Fanta sabor laranja, garrafa 2 L.", "/uploads/produtos/saborviva-fanta-laranja-2l.png", 400)
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
            desativarProdutosForaDoCatalogo(empresa, produtos);
        }

        log.info("Marketplace seed: {} empresas e {} produtos sincronizados.", empresasAtualizadas, produtosAtualizados);
    }

    private int sincronizarProduto(Empresa empresa, ProdutoFix fix) {
        Optional<Produto> existente = produtoRepository.findByCodigo(fix.codigo());

        if (existente.isEmpty()) {
            existente = produtoRepository.findByEmpresaIdAndAtivo(empresa.getId(), 1).stream()
                    .filter(p -> fix.nome().equals(p.getNome()))
                    .findFirst();
        }

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
            novo.setCodigo(fix.codigo());
            novo.setEstoque(BigDecimal.valueOf(fix.estoqueInicial()));
            return novo;
        });

        produto.setCodigo(fix.codigo());
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

    private void desativarProdutosForaDoCatalogo(Empresa empresa, List<ProdutoFix> catalogo) {
        Set<String> nomesOficiais = catalogo.stream()
                .map(ProdutoFix::nome)
                .collect(Collectors.toCollection(HashSet::new));

        produtoRepository.findByEmpresaIdAndAtivo(empresa.getId(), 1).stream()
                .filter(p -> !nomesOficiais.contains(p.getNome()))
                .forEach(p -> {
                    p.setAtivo(0);
                    produtoRepository.save(p);
                    log.info("Marketplace seed: produto extra desativado — {} (empresa {}).", p.getNome(), empresa.getNome());
                });
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
