package com.quickstock.backend.config;

import com.quickstock.backend.entity.Empresa;
import com.quickstock.backend.entity.EnderecoEntrega;
import com.quickstock.backend.entity.Usuario;
import com.quickstock.backend.repository.EmpresaRepository;
import com.quickstock.backend.repository.EnderecoEntregaRepository;
import com.quickstock.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(90)
public class EnderecoSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EnderecoSeedRunner.class);
    private static final List<String> TIPOS_FORNECEDOR = List.of("DISTRIBUIDOR", "PLATAFORMA");

    private final UsuarioRepository usuarioRepository;
    private final EnderecoEntregaRepository enderecoRepository;

    public EnderecoSeedRunner(UsuarioRepository usuarioRepository, EnderecoEntregaRepository enderecoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.enderecoRepository = enderecoRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int criados = 0;
        for (Usuario usuario : usuarioRepository.findAll()) {
            Empresa empresa = usuario.getEmpresa();
            if (empresa == null) continue;
            if (empresa.getTipo() != null && TIPOS_FORNECEDOR.contains(empresa.getTipo())) continue;
            if (enderecoRepository.countByEmpresaId(empresa.getId()) >= 2) continue;
            criados += seedParaEmpresa(empresa);
        }
        if (criados > 0) {
            log.info("Endereco seed: {} endereços demo criados.", criados);
        }
    }

    private int seedParaEmpresa(Empresa empresa) {
        int count = 0;
        if (enderecoRepository.countByEmpresaId(empresa.getId()) == 0) {
            enderecoRepository.save(criarEndereco(
                    empresa, "Depósito", "Rua das Bebidas", "450", "Galpão 2",
                    "Vila Industrial", "São Paulo", "SP", "01310-100", true));
            enderecoRepository.save(criarEndereco(
                    empresa, "Loja centro", "Av. Paulista", "1200", "Loja 3",
                    "Bela Vista", "São Paulo", "SP", "01310-200", false));
            count = 2;
        }
        return count;
    }

    private EnderecoEntrega criarEndereco(
            Empresa empresa, String apelido, String logradouro, String numero, String complemento,
            String bairro, String cidade, String uf, String cep, boolean principal) {
        EnderecoEntrega endereco = new EnderecoEntrega();
        endereco.setEmpresa(empresa);
        endereco.setApelido(apelido);
        endereco.setLogradouro(logradouro);
        endereco.setNumero(numero);
        endereco.setComplemento(complemento);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setUf(uf);
        endereco.setCep(cep);
        endereco.setPrincipal(principal);
        return endereco;
    }
}
