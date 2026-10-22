package com.stock4event.backend.controller;

import com.stock4event.backend.dto.LoginResponseDTO;
import com.stock4event.backend.dto.UsuarioAtualizacaoDTO;
import com.stock4event.backend.dto.UsuarioRegistroDTO;
import com.stock4event.backend.dto.UsuarioResponseDTO;
import com.stock4event.backend.entity.Usuario;
import com.stock4event.backend.exception.CadastroException;
import com.stock4event.backend.repository.EmpresaRepository;
import com.stock4event.backend.repository.PerfilRepository;
import com.stock4event.backend.repository.UsuarioRepository;
import com.stock4event.backend.security.AcessoService;
import com.stock4event.backend.service.JwtService;
import com.stock4event.backend.service.ProdutoUploadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PerfilRepository  perfilRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private JwtService jwtService;
    @Autowired private AcessoService acesso;
    @Autowired private ProdutoUploadService uploadService;

    /** Usuário dono do token (o filtro JWT já validou assinatura, expiração e se está ativo). */
    @GetMapping("/me")
    public ResponseEntity<?> usuarioAtual() {
        return usuarioRepository.findById(acesso.usuarioId())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(new UsuarioResponseDTO(u)))
                .orElse(ResponseEntity.status(401).body(Map.of("erro", "Usuário não encontrado.")));
    }

    /** Usuários da empresa do usuário logado. */
    @GetMapping
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findByEmpresaId(acesso.empresaId())
                .stream()
                .map(UsuarioResponseDTO::new)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscar(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    acesso.exigirEmpresa(u.getEmpresa().getId());
                    return ResponseEntity.ok(new UsuarioResponseDTO(u));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/empresa/{empresaId}")
    public List<UsuarioResponseDTO> listarPorEmpresa(@PathVariable Long empresaId) {
        return usuarioRepository.findByEmpresaId(acesso.resolverEmpresa(empresaId))
                .stream()
                .map(UsuarioResponseDTO::new)
                .toList();
    }

    /** Cria outro usuário na empresa do usuário logado. */
    @PostMapping("/register")
    public ResponseEntity<?> registrar(@Valid @RequestBody UsuarioRegistroDTO body) {
        Long empresaId = acesso.resolverEmpresa(body.getEmpresaId());
        String email = body.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(409).body(Map.of("erro", "E-mail já cadastrado."));
        }

        var perfil = perfilRepository.findById(body.getPerfilId());
        if (perfil.isEmpty()) return ResponseEntity.badRequest().body(Map.of("erro", "Perfil não encontrado."));
        var empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Empresa não encontrada."));

        Usuario u = new Usuario();
        u.setNome(body.getNome().trim());
        u.setEmail(email);
        u.setSenhaHash(BCrypt.hashpw(body.getSenha(), BCrypt.gensalt()));
        u.setPerfil(perfil.get());
        u.setEmpresa(empresa);
        u.setAtivo(1);

        return ResponseEntity.status(201).body(new UsuarioResponseDTO(usuarioRepository.save(u)));
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<?> recuperarSenha(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String novaSenha = body.get("novaSenha");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Informe o e-mail."));
        }
        if (novaSenha == null || novaSenha.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("erro", "A nova senha deve ter pelo menos 6 caracteres."));
        }
        if (novaSenha.length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("erro", "A nova senha deve ter no máximo 100 caracteres."));
        }

        return usuarioRepository.findByEmailIgnoreCase(email.trim())
                .map(u -> {
                    u.setSenhaHash(BCrypt.hashpw(novaSenha, BCrypt.gensalt()));
                    usuarioRepository.save(u);
                    return ResponseEntity.ok(Map.of("mensagem", "Senha atualizada."));
                })
                .orElse(ResponseEntity.status(404).body(Map.of("erro", "E-mail não encontrado.")));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String senha = body.get("senha");

        if (email == null || email.isBlank() || senha == null) {
            return ResponseEntity.status(401).body(Map.of("erro", "E-mail ou senha incorretos."));
        }

        return usuarioRepository.findByEmailIgnoreCase(email.trim())
                .filter(u -> u.getAtivo() == 1)
                .filter(u -> BCrypt.checkpw(senha, u.getSenhaHash()))
                .map(u -> {
                    String token = jwtService.generateToken(u);
                    return ResponseEntity.ok((Object) new LoginResponseDTO(
                            token,
                            jwtService.getExpirationMs(),
                            u
                    ));
                })
                .orElse(ResponseEntity.status(401).body(Map.of("erro", "E-mail ou senha incorretos.")));
    }

    /**
     * Cada usuário altera apenas os próprios dados (nome, e-mail e senha).
     * Perfil de acesso, empresa e status ativo não são alteráveis por aqui.
     */
    /** Envia a foto de perfil do usuário; devolve a URL para usar no PUT (campo fotoUrl). */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFoto(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Arquivo de imagem é obrigatório."));
        }

        try {
            String imagemUrl = uploadService.salvarImagem(file, "usuarios");
            return ResponseEntity.ok(Map.of("imagemUrl", imagemUrl));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Não foi possível salvar a imagem."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @Valid @RequestBody UsuarioAtualizacaoDTO body) {
        if (!id.equals(acesso.usuarioId())) {
            if (!usuarioRepository.existsById(id)) return ResponseEntity.notFound().build();
            throw AcessoService.proibido("Você só pode alterar os seus próprios dados.");
        }

        return usuarioRepository.findById(id).map(u -> {
            if (body.getNome() != null) u.setNome(body.getNome().trim());
            if (body.getEmail() != null) {
                String email = body.getEmail().trim().toLowerCase();
                if (!email.equalsIgnoreCase(u.getEmail()) && usuarioRepository.existsByEmailIgnoreCase(email)) {
                    throw new CadastroException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
                }
                u.setEmail(email);
            }
            if (body.getSenha() != null) {
                u.setSenhaHash(BCrypt.hashpw(body.getSenha(), BCrypt.gensalt()));
            }
            if (ProdutoUploadService.caminhoImagemValido(body.getFotoUrl())) {
                String foto = body.getFotoUrl().trim();
                u.setFotoUrl(foto.isEmpty() ? null : foto);
            }
            return ResponseEntity.ok((Object) new UsuarioResponseDTO(usuarioRepository.save(u)));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Desativa um usuário da mesma empresa (não apaga). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        Usuario u = usuarioRepository.findById(id).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();
        acesso.exigirEmpresa(u.getEmpresa().getId());
        u.setAtivo(0);
        usuarioRepository.save(u);
        return ResponseEntity.noContent().build();
    }
}
