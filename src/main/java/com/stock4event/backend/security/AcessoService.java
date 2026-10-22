package com.stock4event.backend.security;

import com.stock4event.backend.entity.Barraca;
import com.stock4event.backend.entity.Pedido;
import com.stock4event.backend.exception.CadastroException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Fonte única da empresa do usuário logado. A empresa vem sempre do token, nunca do app:
 * cada usuário pertence a uma única empresa (Usuario.empresa), então não há troca de empresa.
 */
@Service
public class AcessoService {

    public static final String EMPRESA_DIFERENTE = "Acesso negado: os dados pertencem a outra empresa.";

    public UsuarioAutenticado atual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario;
        }
        throw new CadastroException(HttpStatus.UNAUTHORIZED, "Autenticação necessária. Faça login novamente.");
    }

    public Long empresaId() {
        return atual().empresaId();
    }

    public Long usuarioId() {
        return atual().id();
    }

    /**
     * Devolve a empresa do usuário logado. Se o app informou outra empresa (parâmetro ou corpo),
     * a requisição é recusada com 403 em vez de ser atendida para a empresa informada.
     */
    public Long resolverEmpresa(Long empresaInformada) {
        Long propria = empresaId();
        if (empresaInformada != null && !empresaInformada.equals(propria)) {
            throw new CadastroException(HttpStatus.FORBIDDEN, EMPRESA_DIFERENTE);
        }
        return propria;
    }

    /** 403 se o registro não pertence à empresa do usuário logado. */
    public void exigirEmpresa(Long empresaDona) {
        if (empresaDona == null || !empresaDona.equals(empresaId())) {
            throw new CadastroException(HttpStatus.FORBIDDEN, EMPRESA_DIFERENTE);
        }
    }

    public static CadastroException proibido(String mensagem) {
        return new CadastroException(HttpStatus.FORBIDDEN, mensagem);
    }

    public static Long empresaDaBarraca(Barraca barraca) {
        return barraca.getEvento().getEmpresa().getId();
    }

    /** Um pedido é da empresa se ela é dona da barraca (PDV) ou é a compradora/fornecedora (marketplace). */
    public boolean podeVerPedido(Pedido pedido) {
        Long e = empresaId();
        return (pedido.getBarraca() != null && e.equals(empresaDaBarraca(pedido.getBarraca())))
                || (pedido.getEmpresaCompradora() != null && e.equals(pedido.getEmpresaCompradora().getId()))
                || (pedido.getEmpresaFornecedora() != null && e.equals(pedido.getEmpresaFornecedora().getId()));
    }

    public void exigirVerPedido(Pedido pedido) {
        if (!podeVerPedido(pedido)) throw proibido(EMPRESA_DIFERENTE);
    }

    /**
     * Alterações diretas em pedido, itens e pagamentos valem só para pedidos de PDV da própria barraca.
     * Pedidos do marketplace mudam apenas pelo fluxo de /api/solicitacoes-compra, que mantém
     * estoque, pagamento e status sincronizados.
     */
    public void exigirPedidoPdvProprio(Pedido pedido) {
        exigirVerPedido(pedido);
        if ("marketplace".equals(pedido.getTipo()) || pedido.getBarraca() == null) {
            throw new CadastroException(HttpStatus.CONFLICT,
                    "Pedidos do marketplace só podem ser alterados pelas solicitações de compra.");
        }
        if (pedido.getChaveVenda() != null) {
            // Venda do PDV: itens, estoque e pagamento já foram baixados juntos e não podem ser editados avulsos.
            throw new CadastroException(HttpStatus.CONFLICT, "Vendas finalizadas no PDV não podem ser alteradas.");
        }
        exigirEmpresa(empresaDaBarraca(pedido.getBarraca()));
    }
}
