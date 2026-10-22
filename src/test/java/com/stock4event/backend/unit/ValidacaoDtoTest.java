package com.stock4event.backend.unit;

import com.stock4event.backend.dto.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Regras de validação dos DTOs, conforme as anotações existentes no código. */
class ValidacaoDtoTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static Set<String> camposInvalidos(Object dto) {
        Set<String> campos = new java.util.TreeSet<>();
        for (ConstraintViolation<Object> v : validator.validate(dto)) {
            campos.add(v.getPropertyPath().toString());
        }
        return campos;
    }

    private static String texto(int tamanho) {
        return "x".repeat(tamanho);
    }

    @Nested
    class Produto {
        ProdutoRequestDTO valido() {
            ProdutoRequestDTO d = new ProdutoRequestDTO();
            d.setNome("Cerveja");
            d.setPrecoVenda(new BigDecimal("5.99"));
            d.setUnidade("UN");
            d.setEstoque(new BigDecimal("10"));
            return d;
        }

        @Test void validoNaoTemErros() { assertThat(camposInvalidos(valido())).isEmpty(); }

        @Test void empresaIdEhOpcionalPoisVemDoToken() {
            ProdutoRequestDTO d = valido();
            d.setEmpresaId(null);
            assertThat(camposInvalidos(d)).isEmpty();
        }

        @Test void nomeObrigatorioENaoVazio() {
            ProdutoRequestDTO d = valido(); d.setNome(null);
            assertThat(camposInvalidos(d)).contains("nome");
            d.setNome("   ");
            assertThat(camposInvalidos(d)).contains("nome");
        }

        @Test void limitesDeTamanho() {
            ProdutoRequestDTO d = valido();
            d.setNome(texto(300)); assertThat(camposInvalidos(d)).isEmpty();
            d.setNome(texto(301)); assertThat(camposInvalidos(d)).contains("nome");
            d = valido(); d.setUnidade(texto(21)); assertThat(camposInvalidos(d)).contains("unidade");
            d = valido(); d.setDescricao(texto(501)); assertThat(camposInvalidos(d)).contains("descricao");
            d = valido(); d.setImagemUrl(texto(501)); assertThat(camposInvalidos(d)).contains("imagemUrl");
        }

        @Test void precoEEstoqueNaoPodemSerNegativos() {
            ProdutoRequestDTO d = valido(); d.setPrecoVenda(null);
            assertThat(camposInvalidos(d)).contains("precoVenda");
            d.setPrecoVenda(new BigDecimal("-0.01"));
            assertThat(camposInvalidos(d)).contains("precoVenda");
            d = valido(); d.setEstoque(new BigDecimal("-1"));
            assertThat(camposInvalidos(d)).contains("estoque");
            d = valido(); d.setPrecoVenda(BigDecimal.ZERO); d.setEstoque(BigDecimal.ZERO);
            assertThat(camposInvalidos(d)).isEmpty();
        }
    }

    @Nested
    class Endereco {
        EnderecoEntregaRequestDTO valido() {
            EnderecoEntregaRequestDTO d = new EnderecoEntregaRequestDTO();
            d.setApelido("Loja"); d.setLogradouro("Rua A"); d.setNumero("1");
            d.setBairro("Centro"); d.setCidade("Caruaru"); d.setUf("PE"); d.setCep("55000-000");
            return d;
        }

        @Test void validoNaoTemErros() { assertThat(camposInvalidos(valido())).isEmpty(); }

        @Test void obrigatorios() {
            EnderecoEntregaRequestDTO d = new EnderecoEntregaRequestDTO();
            assertThat(camposInvalidos(d)).contains("apelido", "logradouro", "numero", "bairro", "cidade", "uf", "cep");
        }

        @Test void limites() {
            EnderecoEntregaRequestDTO d = valido(); d.setUf("PER");
            assertThat(camposInvalidos(d)).containsExactly("uf");
            d = valido(); d.setCep("55000-0000");
            assertThat(camposInvalidos(d)).containsExactly("cep");
            d = valido(); d.setComplemento(texto(81));
            assertThat(camposInvalidos(d)).containsExactly("complemento");
        }
    }

    @Nested
    class Usuario {
        UsuarioRequestDTO valido() {
            UsuarioRequestDTO d = new UsuarioRequestDTO();
            d.setNome("Ana"); d.setEmail("ana@teste.local"); d.setSenha("123456");
            return d;
        }

        @Test void validoNaoTemErros() { assertThat(camposInvalidos(valido())).isEmpty(); }

        @Test void emailInvalido() {
            UsuarioRequestDTO d = valido(); d.setEmail("sem-arroba");
            assertThat(camposInvalidos(d)).containsExactly("email");
        }

        @Test void emailPrecisaDeDominioComPonto() {
            for (String invalido : List.of("ana@semdominio", "ana@dominio.", "ana@.com", "ana @teste.com", "ana@@teste.com")) {
                UsuarioRequestDTO d = valido(); d.setEmail(invalido);
                assertThat(camposInvalidos(d)).as(invalido).containsExactly("email");
            }
            for (String valido : List.of("ana@teste.local", "ana.souza+evento@empresa.com.br")) {
                UsuarioRequestDTO d = valido(); d.setEmail(valido);
                assertThat(camposInvalidos(d)).as(valido).isEmpty();
            }
        }

        @Test void senhaEntre6e100() {
            UsuarioRequestDTO d = valido(); d.setSenha("12345");
            assertThat(camposInvalidos(d)).containsExactly("senha");
            d.setSenha(texto(101));
            assertThat(camposInvalidos(d)).containsExactly("senha");
        }

        @Test void atualizacaoAceitaCamposNulosMasNaoEmBranco() {
            UsuarioAtualizacaoDTO a = new UsuarioAtualizacaoDTO();
            assertThat(camposInvalidos(a)).isEmpty();
            a.setNome("   ");
            a.setEmail("invalido");
            a.setSenha("123");
            assertThat(camposInvalidos(a)).containsExactlyInAnyOrder("nome", "email", "senha");
        }

        @Test void cadastroValidaEmpresaEUsuarioAninhados() {
            CadastroContaRequestDTO c = new CadastroContaRequestDTO();
            assertThat(camposInvalidos(c)).contains("empresa", "usuario");
            EmpresaRequestDTO e = new EmpresaRequestDTO();
            e.setNome(""); e.setCnpj(texto(19));
            c.setEmpresa(e);
            UsuarioRequestDTO u = valido(); u.setEmail("x");
            c.setUsuario(u);
            assertThat(camposInvalidos(c)).contains("empresa.nome", "empresa.cnpj", "usuario.email");
        }
    }

    @Nested
    class SolicitacaoCompra {
        SolicitacaoCompraRequestDTO valida() {
            SolicitacaoCompraRequestDTO d = new SolicitacaoCompraRequestDTO();
            d.setEmpresaFornecedoraId(1L);
            d.setMetodoPagamento("pix");
            d.setEnderecoEntregaId(1L);
            d.setTaxaEntrega(new BigDecimal("7"));
            ItemSolicitacaoRequestDTO item = new ItemSolicitacaoRequestDTO();
            item.setProdutoId(1L); item.setQuantidade(BigDecimal.ONE);
            d.setItens(new ArrayList<>(List.of(item)));
            return d;
        }

        @Test void validaNaoTemErros() { assertThat(camposInvalidos(valida())).isEmpty(); }

        @Test void taxaDeEntregaNegativaEhRecusada() {
            SolicitacaoCompraRequestDTO d = valida(); d.setTaxaEntrega(new BigDecimal("-0.01"));
            assertThat(camposInvalidos(d)).containsExactly("taxaEntrega");
        }

        @Test void quantidadeDoItemPrecisaSerPositiva() {
            SolicitacaoCompraRequestDTO d = valida();
            d.getItens().get(0).setQuantidade(BigDecimal.ZERO);
            assertThat(camposInvalidos(d)).containsExactly("itens[0].quantidade");
            d.getItens().get(0).setQuantidade(null);
            d.getItens().get(0).setProdutoId(null);
            assertThat(camposInvalidos(d)).contains("itens[0].quantidade", "itens[0].produtoId");
        }

        @Test void itemNuloEFornecedorAusenteSaoRecusados() {
            SolicitacaoCompraRequestDTO d = valida();
            d.getItens().add(null);
            d.setEmpresaFornecedoraId(null);
            d.setMetodoPagamento(" ");
            assertThat(camposInvalidos(d)).contains("itens[1].<list element>", "empresaFornecedoraId", "metodoPagamento");
        }

        @Test void observacaoLimitadaA500() {
            SolicitacaoCompraRequestDTO d = valida(); d.setObservacao(texto(501));
            assertThat(camposInvalidos(d)).containsExactly("observacao");
        }
    }

    @Nested
    class Quiosque {
        @Test void itensDoQuiosqueSaoValidados() {
            BarracaRequestDTO d = new BarracaRequestDTO();
            d.setNome("Barraca"); d.setResponsavelId(1L);
            EstoqueItemRequestDTO item = new EstoqueItemRequestDTO();
            item.setProdutoId(null); item.setQuantidade(new BigDecimal("-1"));
            d.setItens(new ArrayList<>(List.of(item)));
            assertThat(camposInvalidos(d)).containsExactlyInAnyOrder("itens[0].produtoId", "itens[0].quantidade");
        }

        @Test void nomeEResponsavelObrigatorios() {
            BarracaRequestDTO d = new BarracaRequestDTO();
            d.setNome(texto(101));
            assertThat(camposInvalidos(d)).containsExactlyInAnyOrder("nome", "responsavelId");
        }
    }

    @Nested
    class Pagamento {
        @Test void cartaoRespeitaLimites() {
            CartaoPagamentoSalvoRequestDTO d = new CartaoPagamentoSalvoRequestDTO();
            d.setTipo("credito"); d.setBandeira("Visa"); d.setTitular("Ana");
            d.setUltimosDigitos("12345"); d.setValidade("12/300");
            assertThat(camposInvalidos(d)).containsExactlyInAnyOrder("ultimosDigitos", "validade");
        }

        @Test void chavePixEFormaObrigatorias() {
            assertThat(camposInvalidos(new ChavePixSalvaRequestDTO()))
                    .containsExactlyInAnyOrder("apelido", "tipoChave", "chaveMascarada", "ultimosDigitos");
            assertThat(camposInvalidos(new FormaPagamentoSalvaRequestDTO()))
                    .containsExactlyInAnyOrder("tipo", "apelido");
        }

        @Test void caminhoneiroObrigatorios() {
            CaminhoneiroRequestDTO d = new CaminhoneiroRequestDTO();
            d.setNome(texto(121));
            assertThat(camposInvalidos(d)).containsExactlyInAnyOrder("nome", "veiculo", "rota");
        }
    }
}
