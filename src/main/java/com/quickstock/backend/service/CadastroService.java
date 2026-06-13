package com.quickstock.backend.service;

import com.quickstock.backend.dto.CadastroContaRequestDTO;
import com.quickstock.backend.entity.Empresa;
import com.quickstock.backend.entity.Perfil;
import com.quickstock.backend.entity.Usuario;
import com.quickstock.backend.exception.CadastroException;
import com.quickstock.backend.repository.EmpresaRepository;
import com.quickstock.backend.repository.PerfilRepository;
import com.quickstock.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CadastroService {

    private static final List<String> PERFIS_ADMIN = List.of("Admin", "Gestor", "Gerente");

    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PerfilRepository perfilRepository;

    @Transactional
    public Usuario cadastrarConta(CadastroContaRequestDTO request) {
        var usuarioDto = request.getUsuario();
        var empresaDto = request.getEmpresa();

        String email = usuarioDto.getEmail().trim().toLowerCase();
        String cnpj = empresaDto.getCnpj().trim();

        if (usuarioRepository.existsByEmail(email)) {
            throw new CadastroException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
        }

        if (empresaRepository.existsByCnpj(cnpj)) {
            throw new CadastroException(HttpStatus.CONFLICT, "CNPJ já cadastrado.");
        }

        Perfil perfilAdmin = buscarPerfilAdmin()
                .orElseThrow(() -> new CadastroException(
                        HttpStatus.BAD_REQUEST,
                        "Perfil de administrador não encontrado no sistema."
                ));

        Empresa empresa = new Empresa();
        empresa.setNome(empresaDto.getNome().trim());
        empresa.setCnpj(cnpj);
        if (empresaDto.getTelefone() != null && !empresaDto.getTelefone().isBlank()) {
            empresa.setTelefone(empresaDto.getTelefone().trim());
        }
        empresa = empresaRepository.save(empresa);

        String hash = BCrypt.hashpw(usuarioDto.getSenha(), BCrypt.gensalt());

        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDto.getNome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(hash);
        usuario.setPerfil(perfilAdmin);
        usuario.setEmpresa(empresa);
        usuario.setAtivo(1);

        return usuarioRepository.save(usuario);
    }

    private java.util.Optional<Perfil> buscarPerfilAdmin() {
        for (String nome : PERFIS_ADMIN) {
            var perfil = perfilRepository.findByNomeIgnoreCase(nome);
            if (perfil.isPresent()) {
                return perfil;
            }
        }
        return java.util.Optional.empty();
    }
}
