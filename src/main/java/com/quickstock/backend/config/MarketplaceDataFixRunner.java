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

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class MarketplaceDataFixRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceDataFixRunner.class);
    private static final List<String> TIPOS_FORNECEDOR = List.of("DISTRIBUIDOR", "PLATAFORMA");

    private final EmpresaRepository empresaRepository;
    private final ProdutoRepository produtoRepository;

    public MarketplaceDataFixRunner(
            EmpresaRepository empresaRepository,
            ProdutoRepository produtoRepository) {
        this.empresaRepository = empresaRepository;
        this.produtoRepository = produtoRepository;
    }

    private record EmpresaFix(String nome, String descricao, String logoUrl, String capaUrl) {}

    private record ProdutoFix(String nome, String descricao, String imagemUrl) {}

    /** Logos via ui-avatars e capas via picsum — URLs estáveis (Unsplash antigo retorna 404). */
    private static String logo(String initials) {
        return "https://ui-avatars.com/api/?name=" + initials
                + "&background=F8B125&color=fff&bold=true&size=200&format=png";
    }

    private static String capa(String seed) {
        return "https://picsum.photos/seed/" + seed + "/800/400";
    }

    private static final Map<String, EmpresaFix> EMPRESAS = Map.ofEntries(
            Map.entry("11.111.111/0001-11", new EmpresaFix(
                    "QuickStock Distribuidora",
                    "Distribuidora oficial QuickStock. Cervejas, refrigerantes e \u00e1guas para revenda em todo o Nordeste.",
                    logo("QS"),
                    capa("quickstock-capa"))),
            Map.entry("22.222.222/0001-22", new EmpresaFix(
                    "Tonio Distribuidora",
                    "Atacado de bebidas em Caruaru. Cervejas nacionais e geladas para bares, quiosques e eventos.",
                    logo("TD"),
                    capa("tonio-capa"))),
            Map.entry("33.333.333/0001-33", new EmpresaFix(
                    "Nordeste Bebidas Atacado",
                    "Atacado de cervejas, refrigerantes e energ\u00e9ticos para revenda no Nordeste.",
                    logo("NB"),
                    capa("nordeste-capa"))),
            Map.entry("44.444.444/0001-44", new EmpresaFix(
                    "Imperial Cervejas PE",
                    "Distribuidora premium de cervejas importadas e nacionais em Pernambuco.",
                    logo("IP"),
                    capa("imperial-capa"))),
            Map.entry("55.555.555/0001-55", new EmpresaFix(
                    "Gelada Express Caruaru",
                    "Entrega r\u00e1pida de bebidas geladas para bares, quiosques e festas em Caruaru.",
                    logo("GE"),
                    capa("gelada-capa"))),
            Map.entry("66.666.666/0001-66", new EmpresaFix(
                    "Refri & Cia Distribuidora",
                    "Refrigerantes, sucos e \u00e1guas para revenda em grande volume.",
                    logo("RC"),
                    capa("refri-capa"))),
            Map.entry("77.777.777/0001-77", new EmpresaFix(
                    "Festa Drinks Atacado",
                    "Bebidas para festas juninas, eventos e feiras de Caruaru.",
                    logo("FD"),
                    capa("festa-capa"))),
            Map.entry("88.888.888/0001-88", new EmpresaFix(
                    "Porto Breja Distribuidora",
                    "Especialista em cervejas craft e chopes para bares e restaurantes.",
                    logo("PB"),
                    capa("porto-capa"))),
            Map.entry("99.999.999/0001-99", new EmpresaFix(
                    "\u00c1gua & G\u00e1s Nordeste",
                    "\u00c1guas minerais, g\u00e1s de cozinha e bebidas n\u00e3o alco\u00f3licas para revenda.",
                    logo("AG"),
                    capa("agua-capa"))),
            Map.entry("10.101.010/0001-10", new EmpresaFix(
                    "Bira Premium Atacado",
                    "Cervejas premium, long necks e packs para atacado e revenda.",
                    logo("BP"),
                    capa("bira-capa"))),
            Map.entry("20.202.020/0001-20", new EmpresaFix(
                    "S\u00e3o Jo\u00e3o Bebidas",
                    "Fornecedor tradicional de bebidas para festas de S\u00e3o Jo\u00e3o e eventos sazonais.",
                    logo("SJ"),
                    capa("saojoao-capa")))
    );

    private static final List<ProdutoFix> PRODUTOS = List.of(
            new ProdutoFix("Skol Lata 350ml",
                    "Cerveja Skol Pilsen em lata de 350 ml. Ideal para revenda em quiosques e bares.",
                    "https://images.unsplash.com/photo-1618180034764-13277f148d92?w=400&h=400&fit=crop&bg=ffffff"),
            new ProdutoFix("Brahma 600ml",
                    "Cerveja Brahma long neck 600 ml. Bebida gelada para pontos de venda.",
                    "https://images.unsplash.com/photo-1523367868498-57a314dc45a5?w=400&h=400&fit=crop&bg=ffffff"),
            new ProdutoFix("Heineken Long Neck",
                    "Cerveja Heineken long neck 330 ml. Linha premium para bares e restaurantes.",
                    "https://images.unsplash.com/photo-1618885472179-5e4740f0882c?w=400&h=400&fit=crop&bg=ffffff"),
            new ProdutoFix("Stella Artois 330ml",
                    "Cerveja Stella Artois long neck 330 ml. Importada, ideal para revenda premium.",
                    "https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=400&h=400&fit=crop&bg=ffffff"),
            new ProdutoFix("Corona Extra 330ml",
                    "Cerveja Corona Extra long neck 330 ml. Refrescante para revenda em eventos.",
                    "https://images.unsplash.com/photo-1603847709497-37f1370d1c68?w=400&h=400&fit=crop&bg=ffffff"),
            new ProdutoFix("Antarctica Original 600ml",
                    "Cerveja Antarctica Original long neck 600 ml. Sabor tradicional para revenda.",
                    "https://images.unsplash.com/photo-1523367868498-57a314dc45a5?w=400&h=400&fit=crop&bg=ffffff"),
            new ProdutoFix("Coca-Cola 2L",
                    "Refrigerante Coca-Cola pet 2 litros. Produto de alta rotatividade para revenda.",
                    "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400&h=400&fit=crop&bg=ffffff"),
            new ProdutoFix("Guaran\u00e1 Antarctica 2L",
                    "Refrigerante Guaran\u00e1 Antarctica pet 2 litros. Sabor guaran\u00e1 para revenda.",
                    "https://images.unsplash.com/photo-1625777453046-048aedc1e009?w=400&h=400&fit=crop&bg=ffffff"),
            new ProdutoFix("Fanta Laranja 2L",
                    "Refrigerante Fanta Laranja pet 2 litros. Sabor laranja para revenda.",
                    "https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400&h=400&fit=crop&bg=ffffff"),
            new ProdutoFix("\u00c1gua Crystal 5L",
                    "\u00c1gua mineral Crystal sem g\u00e1s, gal\u00e3o de 5 litros. Essencial para revenda.",
                    "https://images.unsplash.com/photo-1563636619777-756a77877439?w=400&h=400&fit=crop&bg=ffffff"),
            new ProdutoFix("\u00c1gua Crystal 1,5L",
                    "\u00c1gua mineral Crystal sem g\u00e1s, garrafa de 1,5 litros.",
                    "https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400&h=400&fit=crop&bg=ffffff"),
            new ProdutoFix("Red Bull 250ml",
                    "Energ\u00e9tico Red Bull lata 250 ml. Alta demanda em bares e eventos.",
                    "https://images.unsplash.com/photo-1622543925917-763c38e03a39?w=400&h=400&fit=crop&bg=ffffff"),
            new ProdutoFix("Suco Del Valle 1L",
                    "Suco Del Valle sabor laranja, caixa de 1 litro. Op\u00e7\u00e3o n\u00e3o alco\u00f3lica para revenda.",
                    "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&h=400&fit=crop&bg=ffffff")
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int empresasAtualizadas = 0;
        Set<Long> idsAtualizados = new java.util.HashSet<>();

        for (Map.Entry<String, EmpresaFix> entry : EMPRESAS.entrySet()) {
            var empresaOpt = empresaRepository.findByCnpj(entry.getKey());
            if (empresaOpt.isPresent()) {
                aplicarFixEmpresa(empresaOpt.get(), entry.getValue());
                idsAtualizados.add(empresaOpt.get().getId());
                empresasAtualizadas++;
            } else {
                log.warn("Marketplace seed: CNPJ {} não encontrado no banco.", entry.getKey());
            }
        }

        for (Empresa empresa : empresaRepository.findAll()) {
            String tipo = empresa.getTipo();
            if (tipo == null || !TIPOS_FORNECEDOR.contains(tipo)) {
                continue;
            }
            if (idsAtualizados.contains(empresa.getId())) {
                continue;
            }
            if (!precisaCorrigirImagens(empresa)) {
                continue;
            }
            EmpresaFix fix = EMPRESAS.get(empresa.getCnpj());
            if (fix != null) {
                aplicarFixEmpresa(empresa, fix);
                empresasAtualizadas++;
            } else {
                log.warn(
                        "Marketplace seed: fornecedora id={} cnpj={} sem imagens e fora do mapa de correção.",
                        empresa.getId(),
                        empresa.getCnpj());
            }
        }

        log.info("Marketplace seed: {} empresas atualizadas.", empresasAtualizadas);

        for (ProdutoFix fix : PRODUTOS) {
            List<Produto> matches = produtoRepository.findAll().stream()
                    .filter(p -> correspondeProduto(p.getNome(), fix.nome()))
                    .toList();
            for (Produto produto : matches) {
                produto.setNome(fix.nome());
                produto.setDescricao(fix.descricao());
                produto.setImagemUrl(fix.imagemUrl());
                produtoRepository.save(produto);
            }
        }
    }

    private void aplicarFixEmpresa(Empresa empresa, EmpresaFix fix) {
        empresa.setNome(fix.nome());
        empresa.setDescricao(fix.descricao());
        empresa.setLogoUrl(fix.logoUrl());
        empresa.setCapaUrl(fix.capaUrl());
        empresaRepository.save(empresa);
    }

    private boolean precisaCorrigirImagens(Empresa empresa) {
        if (empresa.getLogoUrl() == null || empresa.getLogoUrl().isBlank()) {
            return true;
        }
        if (empresa.getCapaUrl() == null || empresa.getCapaUrl().isBlank()) {
            return true;
        }
        return empresa.getLogoUrl().contains("images.unsplash.com")
                || empresa.getCapaUrl().contains("images.unsplash.com");
    }

    private boolean correspondeProduto(String nomeAtual, String nomeCorreto) {
        if (nomeAtual == null) return false;
        if (nomeAtual.equals(nomeCorreto)) return true;
        String normalizado = nomeAtual.replaceAll("[^a-zA-Z0-9, ]", "").toLowerCase();
        String alvo = nomeCorreto.replaceAll("[^a-zA-Z0-9, ]", "").toLowerCase();
        return normalizado.equals(alvo);
    }
}
