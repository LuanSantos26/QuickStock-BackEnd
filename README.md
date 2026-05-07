# QuickStock - Back-End 🚀

Este é o módulo de back-end da aplicação **QuickStock**, desenvolvido como projeto acadêmico para gerenciamento inteligente. O servidor foi construído utilizando Java com o ecossistema Spring Boot, focado em fornecer uma API REST para os cadastros de empresas e empreendedores.

## 🛠️ Tecnologias Utilizadas

* **Java 17**: Linguagem principal do projeto.
* **Spring Boot 3.4.0**: Framework para agilizar o desenvolvimento da API.
* **Spring Data JPA**: Para persistência de dados e integração com banco de dados.
* **H2 Database**: Banco de dados em memória utilizado para desenvolvimento e testes rápidos.
* **Lombok**: Biblioteca para redução de código boilerplate (Getters/Setters).
* **Maven**: Gerenciador de dependências e build do projeto.

## 📋 Funcionalidades Atuais

* **Cadastro de Empresas**: Endpoint para receber e salvar dados de novas empresas (CNPJ, Nome, E-mail, etc).
* **Cadastro de Usuários (Empreendedores)**: Endpoint para registro de usuários individuais.
* **Persistência Automática**: Criação automática de tabelas no banco de dados através do Hibernate.

## 🚀 Como Executar o Projeto

### Pré-requisitos
* JDK 17 instalado.
* Maven instalado (ou utilizar o wrapper `./mvnw` incluso).
* Uma IDE (IntelliJ IDEA recomendada).

### Passo a Passo
1.  Clone o repositório:
    ```bash
    git clone [https://github.com/seu-usuario/quickstock-backend.git](https://github.com/seu-usuario/quickstock-backend.git)
    ```
2.  Importe o projeto na sua IDE como um projeto Maven.
3.  Aguarde o download das dependências.
4.  Execute a classe principal: `com.quickstock.backend.BackendApplication`.
5.  O servidor iniciará na porta **8080**.

## 🔌 Endpoints da API

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| POST | `/api/companies/register` | Cadastra uma nova empresa |
| POST | `/api/users/register` | Cadastra um novo empreendedor |

### Exemplo de JSON para Cadastro (POST)
```json
{
  "name": "Exemplo Empresa",
  "email": "contato@exemplo.com",
  "password": "senha123",
  "cnpj": "00.000.000/0001-00",
  "phone": "8199999999"
}
