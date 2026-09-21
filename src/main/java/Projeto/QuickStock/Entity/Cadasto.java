package Projeto.QuickStock.Entity;

import jakarta.persistence.Entity;

@Entity
public class Cadasto {
    private String Nome;
    private String Email;
    private String Senha;
    private String Telefone;
    private String cnpj;
    private Escolhar Fornecedor;
    private Escolhar Cliente;

    public Cadasto() {
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getNome() {
        return Nome;
    }

    public void setNome(String nome) {
        Nome = nome;
    }

    public String getSenha() {
        return Senha;
    }

    public void setSenha(String senha) {
        Senha = senha;
    }

    public String getTelefone() {
        return Telefone;
    }

    public void setTelefone(String telefone) {
        Telefone = telefone;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public Escolhar getFornecedor() {
        return Fornecedor;
    }

    public void setFornecedor(Escolhar fornecedor) {
        Fornecedor = fornecedor;
    }

    public Escolhar getCliente() {
        return Cliente;
    }

    public void setCliente(Escolhar cliente) {
        Cliente = cliente;
    }
}
