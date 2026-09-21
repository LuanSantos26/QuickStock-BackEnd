package Projeto.QuickStock.Controller;

import Projeto.QuickStock.Entity.Cadastro;
import Projeto.QuickStock.Server.CadastroServer;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cadastro")
public class cadastroController {
    CadastroServer cadastroServer;

    public cadastroController(CadastroServer cadastroServer) {
        this.cadastroServer = cadastroServer;
    }
    @PostMapping
    public Cadastro criarCadastro(Cadastro cadastro) {
        return cadastroServer.criarCadastro(cadastro);
    }
    @GetMapping
    public List<Cadastro> getTodosCadastros() {
        return cadastroServer.getTodosCadastros();
    }
    @PutMapping("/{id}")
    public Cadastro atualizarCadastro(@PathVariable Long id, @RequestBody Cadastro cadastro) {
        return cadastroServer.atualizarCadastro(id, cadastro).orElseThrow(() -> new RuntimeException("Cadastro não encontrado"));
    }
    @DeleteMapping("/{id}")
    public void deletarCadastro(@PathVariable Long id) {
        cadastroServer.deletarCadastro(id);
}}
