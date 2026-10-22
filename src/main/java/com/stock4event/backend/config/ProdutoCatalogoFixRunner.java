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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class ProdutoCatalogoFixRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProdutoCatalogoFixRunner.class);

    private record CatalogoDef(
            long idFixo,
            String codigo,
            String cnpj,
            String nome,
            BigDecimal preco,
            String descricao,
            String imagemUrl,
            int estoqueInicial
    ) {}

    private static final List<CatalogoDef> CATALOGO = List.of(
            // Casa dos Vinhos — bebidas sem álcool
            new CatalogoDef(1046, "MKT-CDV-006", "30.001.001/0001-01",
                    "Suco de uva integral Serra Gaúcha 1 L", bd("17.90"),
                    "Linha Acessível · origem Brasil. Bebida sem álcool.",
                    "/uploads/produtos/cdv-suco-de-uva-integral-serra-gaucha-1-l.png", 120),
            new CatalogoDef(1047, "MKT-CDV-007", "30.001.001/0001-01",
                    "Suco de uva branca integral 1 L", bd("19.90"),
                    "Linha Acessível · origem Brasil. Bebida sem álcool.",
                    "/uploads/produtos/cdv-suco-de-uva-branca-integral-1-l.png", 90),
            new CatalogoDef(1048, "MKT-CDV-008", "30.001.001/0001-01",
                    "Néctar de uva premium 1 L", bd("14.90"),
                    "Linha Acessível · origem Brasil. Bebida sem álcool.",
                    "/uploads/produtos/cdv-nectar-de-uva-premium-1-l.png", 150),
            new CatalogoDef(1049, "MKT-CDV-009", "30.001.001/0001-01",
                    "Espumante de uva sem álcool 660 ml", bd("24.90"),
                    "Linha Acessível · origem Brasil. Bebida sem álcool.",
                    "/uploads/produtos/cdv-espumante-de-uva-sem-alcool-660-ml.png", 80),
            new CatalogoDef(1050, "MKT-CDV-010", "30.001.001/0001-01",
                    "Suco de uva rosé integral 1 L", bd("22.90"),
                    "Linha Acessível · origem Brasil. Bebida sem álcool.",
                    "/uploads/produtos/cdv-suco-de-uva-rose-integral-1-l.png", 70),
            new CatalogoDef(1051, "MKT-CDV-011", "30.001.001/0001-01",
                    "Vinho tinto desalcoolizado 750 ml", bd("49.90"),
                    "Linha Intermediário · origem Brasil. Bebida sem álcool.",
                    "/uploads/produtos/cdv-vinho-tinto-desalcoolizado-750-ml.png", 65),
            new CatalogoDef(1052, "MKT-CDV-012", "30.001.001/0001-01",
                    "Vinho branco desalcoolizado 750 ml", bd("54.90"),
                    "Linha Intermediário · origem Brasil. Bebida sem álcool.",
                    "/uploads/produtos/cdv-vinho-branco-desalcoolizado-750-ml.png", 55),
            new CatalogoDef(1053, "MKT-CDV-013", "30.001.001/0001-01",
                    "Espumante brut sem álcool 750 ml", bd("59.90"),
                    "Linha Intermediário · origem Brasil. Bebida sem álcool.",
                    "/uploads/produtos/cdv-espumante-brut-sem-alcool-750-ml.png", 60),
            new CatalogoDef(1054, "MKT-CDV-014", "30.001.001/0001-01",
                    "Suco de uva orgânico reserva 750 ml", bd("39.90"),
                    "Linha Intermediário · origem Brasil. Bebida sem álcool.",
                    "/uploads/produtos/cdv-suco-de-uva-organico-reserva-750-ml.png", 75),
            new CatalogoDef(1055, "MKT-CDV-015", "30.001.001/0001-01",
                    "Vinho rosé desalcoolizado 750 ml", bd("79.90"),
                    "Linha Intermediário · origem Espanha. Bebida sem álcool.",
                    "/uploads/produtos/cdv-vinho-rose-desalcoolizado-750-ml.png", 40),
            new CatalogoDef(1056, "MKT-CDV-016", "30.001.001/0001-01",
                    "Espumante rosé sem álcool 750 ml", bd("119.90"),
                    "Linha Premium · origem França. Bebida sem álcool.",
                    "/uploads/produtos/cdv-espumante-rose-sem-alcool-750-ml.png", 30),
            new CatalogoDef(1057, "MKT-CDV-017", "30.001.001/0001-01",
                    "Vinho tinto desalcoolizado reserva 750 ml", bd("129.90"),
                    "Linha Premium · origem Espanha. Bebida sem álcool.",
                    "/uploads/produtos/cdv-vinho-tinto-desalcoolizado-reserva-750-ml.png", 25),
            new CatalogoDef(1058, "MKT-CDV-018", "30.001.001/0001-01",
                    "Vinho branco desalcoolizado Chardonnay 750 ml", bd("139.90"),
                    "Linha Premium · origem Alemanha. Bebida sem álcool.",
                    "/uploads/produtos/cdv-vinho-branco-desalcoolizado-chardonnay-750-ml.png", 22),
            new CatalogoDef(1059, "MKT-CDV-019", "30.001.001/0001-01",
                    "Espumante sem álcool cuvée 750 ml", bd("149.90"),
                    "Linha Premium · origem Itália. Bebida sem álcool.",
                    "/uploads/produtos/cdv-espumante-sem-alcool-cuvee-750-ml.png", 18),
            new CatalogoDef(1060, "MKT-CDV-020", "30.001.001/0001-01",
                    "Vinho tinto desalcoolizado premium 750 ml", bd("169.90"),
                    "Linha Premium · origem França. Bebida sem álcool.",
                    "/uploads/produtos/cdv-vinho-tinto-desalcoolizado-premium-750-ml.png", 15),
            new CatalogoDef(1011, "MKT-CC-001", "30.002.002/0001-02",
                    "Cerveja Heineken Long Neck 330ml", bd("5.99"),
                    "Cerveja Heineken long neck 330 ml.",
                    "/uploads/produtos/cerveja-heineken-long-neck.png", 200),
            new CatalogoDef(1012, "MKT-CC-002", "30.002.002/0001-02",
                    "Cerveja Corona Long Neck", bd("6.99"),
                    "Cerveja Corona Extra long neck 330 ml.",
                    "/uploads/produtos/cerveja-corona-long-neck.png", 150),
            new CatalogoDef(1013, "MKT-CC-003", "30.002.002/0001-02",
                    "Cerveja Amstel Lata 269ml", bd("3.19"),
                    "Cerveja Amstel lata 269 ml.",
                    "/uploads/produtos/cerveja-amstel-lata.png", 300),
            new CatalogoDef(1014, "MKT-CC-004", "30.002.002/0001-02",
                    "Cerveja Cerpa Export Long Neck 350ml", bd("8.39"),
                    "Cerveja Cerpa Export long neck 350 ml.",
                    "/uploads/produtos/cerveja-cerpa-export-long-neck.png", 100),
            new CatalogoDef(1015, "MKT-CC-005", "30.002.002/0001-02",
                    "Cerveja Paulaner Munchen Weissbier 500ml", bd("11.19"),
                    "Cerveja Paulaner Munchen Weissbier garrafa 500 ml.",
                    "/uploads/produtos/cerveja-paulaner-munchen-weissbier.png", 80),

            new CatalogoDef(1021, "MKT-WL-001", "30.003.003/0001-03",
                    "Whisky Escocês Royal Salute 21 Anos The Signature Blend 700ml", bd("989.00"),
                    "Whisky escocês Royal Salute 21 anos, garrafa 700 ml.",
                    "/uploads/produtos/whisky-royal-salute-21.png", 150),
            new CatalogoDef(1022, "MKT-WL-002", "30.003.003/0001-03",
                    "Whisky The Balvenie PortWood 21 Anos 700ml Single Malt Escocês", bd("2795.30"),
                    "Whisky single malt The Balvenie PortWood 21 anos, garrafa 700 ml.",
                    "/uploads/produtos/whisky-balvenie-portwood-21.png", 100),
            new CatalogoDef(1023, "MKT-WL-003", "30.003.003/0001-03",
                    "Whisky Glenfiddich Single Malt 12 Anos 750ml Escocês", bd("415.75"),
                    "Whisky single malt Glenfiddich 12 anos, garrafa 750 ml.",
                    "/uploads/produtos/whisky-glenfiddich-12.png", 300),
            new CatalogoDef(1024, "MKT-WL-004", "30.003.003/0001-03",
                    "Conhaque Martell L'Or de Jean Martell 700ml", bd("18711.00"),
                    "Conhaque Martell L'Or de Jean Martell, garrafa 700 ml.",
                    "/uploads/produtos/conhaque-martell-lor.png", 80),
            new CatalogoDef(1025, "MKT-WL-005", "30.003.003/0001-03",
                    "Whisky Suntory Hibiki Japanese Harmony 700ml | Blended Premium Japonês", bd("729.90"),
                    "Whisky japonês Suntory Hibiki Japanese Harmony, garrafa 700 ml.",
                    "/uploads/produtos/whisky-hibiki-japanese-harmony.png", 200),
            // SaborViva — águas, sucos e refrigerantes
            new CatalogoDef(1031, "MKT-SV-001", "30.004.004/0001-04",
                    "Água mineral sem gás 500 ml", bd("2.50"),
                    "Água mineral natural sem gás, garrafa 500 ml.",
                    "/uploads/produtos/saborviva-agua-sem-gas-500ml.png", 500),
            new CatalogoDef(1032, "MKT-SV-002", "30.004.004/0001-04",
                    "Água mineral com gás 500 ml", bd("3.50"),
                    "Água mineral com gás, garrafa 500 ml.",
                    "/uploads/produtos/saborviva-agua-com-gas-500ml.png", 350),
            new CatalogoDef(1033, "MKT-SV-003", "30.004.004/0001-04",
                    "Água mineral sem gás 1,5 L", bd("5.00"),
                    "Água mineral natural sem gás, garrafa 1,5 L.",
                    "/uploads/produtos/saborviva-agua-sem-gas-1-5l.png", 250),
            new CatalogoDef(1034, "MKT-SV-004", "30.004.004/0001-04",
                    "Água mineral galão 5 L", bd("12.00"),
                    "Água mineral natural sem gás, galão 5 L.",
                    "/uploads/produtos/saborviva-agua-galao-5l.png", 100),
            new CatalogoDef(1035, "MKT-SV-005", "30.004.004/0001-04",
                    "Água de coco 330 ml", bd("6.50"),
                    "Água de coco natural, caixinha 330 ml.",
                    "/uploads/produtos/saborviva-agua-de-coco-330ml.png", 180),
            new CatalogoDef(1036, "MKT-SV-006", "30.004.004/0001-04",
                    "Suco de laranja 1 L", bd("9.90"),
                    "Suco de laranja 100% fruta, caixa 1 L.",
                    "/uploads/produtos/saborviva-suco-laranja-1l.png", 200),
            new CatalogoDef(1037, "MKT-SV-007", "30.004.004/0001-04",
                    "Suco de uva integral 1 L", bd("14.90"),
                    "Suco de uva integral 100% fruta, caixa 1 L.",
                    "/uploads/produtos/saborviva-suco-uva-integral-1l.png", 150),
            new CatalogoDef(1038, "MKT-SV-008", "30.004.004/0001-04",
                    "Suco de maçã 1 L", bd("10.50"),
                    "Suco de maçã 100% fruta, caixa 1 L.",
                    "/uploads/produtos/saborviva-suco-maca-1l.png", 180),
            new CatalogoDef(1039, "MKT-SV-009", "30.004.004/0001-04",
                    "Suco de manga 1 L", bd("8.90"),
                    "Suco de manga 100% fruta, caixa 1 L.",
                    "/uploads/produtos/saborviva-suco-manga-1l.png", 220),
            new CatalogoDef(1040, "MKT-SV-010", "30.004.004/0001-04",
                    "Suco de maracujá 1 L", bd("9.50"),
                    "Suco de maracujá 100% fruta, caixa 1 L.",
                    "/uploads/produtos/saborviva-suco-maracuja-1l.png", 180),
            new CatalogoDef(1041, "MKT-SV-011", "30.004.004/0001-04",
                    "Coca-Cola Original 2 L", bd("11.90"),
                    "Refrigerante Coca-Cola sabor original, garrafa 2 L.",
                    "/uploads/produtos/saborviva-coca-cola-original-2l.png", 450),
            new CatalogoDef(1042, "MKT-SV-012", "30.004.004/0001-04",
                    "Coca-Cola Zero 2 L", bd("11.90"),
                    "Refrigerante Coca-Cola zero açúcar, garrafa 2 L.",
                    "/uploads/produtos/saborviva-coca-cola-zero-2l.png", 300),
            new CatalogoDef(1043, "MKT-SV-013", "30.004.004/0001-04",
                    "Guaraná Antarctica 2 L", bd("9.50"),
                    "Refrigerante Guaraná Antarctica, garrafa 2 L.",
                    "/uploads/produtos/saborviva-guarana-antarctica-2l.png", 500),
            new CatalogoDef(1044, "MKT-SV-014", "30.004.004/0001-04",
                    "Sprite 2 L", bd("9.90"),
                    "Refrigerante Sprite sabor limão, garrafa 2 L.",
                    "/uploads/produtos/saborviva-sprite-2l.png", 400),
            new CatalogoDef(1045, "MKT-SV-015", "30.004.004/0001-04",
                    "Fanta Laranja 2 L", bd("9.50"),
                    "Refrigerante Fanta sabor laranja, garrafa 2 L.",
                    "/uploads/produtos/saborviva-fanta-laranja-2l.png", 400)
    );

    private final EmpresaRepository empresaRepository;
    private final ProdutoRepository produtoRepository;
    private final JdbcTemplate jdbcTemplate;

    public ProdutoCatalogoFixRunner(
            EmpresaRepository empresaRepository,
            ProdutoRepository produtoRepository,
            JdbcTemplate jdbcTemplate) {
        this.empresaRepository = empresaRepository;
        this.produtoRepository = produtoRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        garantirCodigosLegados();
        int sincronizados = 0;

        for (CatalogoDef def : CATALOGO) {
            Optional<Empresa> empresaOpt = empresaRepository.findByCnpj(def.cnpj());
            if (empresaOpt.isEmpty()) {
                continue;
            }
            sincronizarProduto(empresaOpt.get(), def);
            sincronizados++;
        }

        ajustarSequenciaIds();
        log.info("Catálogo fixo: {} produtos de parceiros sincronizados.", sincronizados);
    }

    private void garantirCodigosLegados() {
        produtoRepository.findAll().stream()
                .filter(p -> p.getCodigo() == null || p.getCodigo().isBlank())
                .forEach(p -> {
                    p.setCodigo("LEG-" + p.getId());
                    produtoRepository.save(p);
                });
    }

    private void sincronizarProduto(Empresa empresa, CatalogoDef def) {
        Optional<Produto> existente = produtoRepository.findByCodigo(def.codigo());
        if (existente.isEmpty()) {
            existente = produtoRepository.findById(def.idFixo());
        }
        if (existente.isEmpty()) {
            existente = produtoRepository.findByEmpresaIdAndAtivo(empresa.getId(), 1).stream()
                    .filter(p -> def.nome().equals(p.getNome()))
                    .findFirst();
        }

        if (existente.isPresent()) {
            Produto produto = existente.get();
            produto.setCodigo(def.codigo());
            produto.setNome(def.nome());
            produto.setDescricao(def.descricao());
            produto.setPrecoVenda(def.preco());
            produto.setImagemUrl(def.imagemUrl());
            produto.setUnidade("UN");
            produto.setAtivo(1);
            if (produto.getEstoque() == null) {
                produto.setEstoque(BigDecimal.valueOf(def.estoqueInicial()));
            }
            produtoRepository.save(produto);
            return;
        }

        jdbcTemplate.update("""
                INSERT INTO produtos (id, codigo, empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo, estoque)
                VALUES (?, ?, ?, ?, ?, 'UN', ?, ?, 1, ?)
                ON CONFLICT (id) DO NOTHING
                """,
                def.idFixo(),
                def.codigo(),
                empresa.getId(),
                def.nome(),
                def.preco(),
                def.descricao(),
                def.imagemUrl(),
                BigDecimal.valueOf(def.estoqueInicial()));
    }

    private void ajustarSequenciaIds() {
        jdbcTemplate.execute("""
                SELECT setval(
                    pg_get_serial_sequence('produtos', 'id'),
                    GREATEST(COALESCE((SELECT MAX(id) FROM produtos), 1), 1025)
                )
                """);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
