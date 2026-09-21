package Projeto.QuickStock.Server;

import Projeto.QuickStock.Repository.CadastroRepository;
import Projeto.QuickStock.Entity.Cadastro;

import java.util.List;
import java.util.Optional;

public class CadastroServer {
    private final CadastroRepository cadastroRepository;

    public CadastroServer(CadastroRepository cadastroRepository) {
        this.cadastroRepository = cadastroRepository;
    }

    // Criar um novo cadastro
    public Cadastro criarCadastro(Cadastro cadastro) {
        return cadastroRepository.save(cadastro);
    }

    // Buscar cadastros pelo nome
    public List<Cadastro> getCadastroPorNome(String nome) {
        return cadastroRepository.findAll()
                .stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome))
                .toList();
    }

    // Buscar todos os cadastros
    public List<Cadastro> getTodosCadastros() {
        return cadastroRepository.findAll();
    }

    // Atualizar cadastro existente
    public Optional<Cadastro> atualizarCadastro(Long id, Cadastro novoCadastro) {
        return cadastroRepository.findById(id).map(cadastroExistente -> {
            cadastroExistente.setNome(novoCadastro.getNome());
            cadastroExistente.setEmail(novoCadastro.getEmail());
            cadastroExistente.setTelefone(novoCadastro.getTelefone());
            // adicione outros campos conforme sua entidade
            return cadastroRepository.save(cadastroExistente);
        });
    }

    // Deletar cadastro por ID
    public void deletarCadastro(Long id) {
        cadastroRepository.deleteById(id);
    }
}
