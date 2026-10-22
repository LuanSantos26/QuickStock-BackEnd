[readme_md.md](https://github.com/user-attachments/files/33796925/readme_md.md)
# 📦 Stock4Event API Backend

O **Stock4Event** é uma plataforma completa e modular projetada para o gerenciamento de estoque, vendas de PDV (eventos/barracas), marketplace B2B (compradores e fornecedores), acompanhamento logístico e controle financeiro.

Esta API foi desenvolvida seguindo os padrões RESTful, utilizando uma arquitetura em camadas escalável e segura.

## ✨ Principais Funcionalidades

### 👥 Gestão de Usuários e Segurança

* Cadastro de empresas com diferentes perfis: **Cliente**, **Fornecedor** ou **Ambos**.
* Autenticação e autorização seguras via **JWT (JSON Web Token)**.
* Criptografia de senhas utilizando **BCrypt**.
* Fluxo de recuperação de senhas.

### 🏪 Marketplace B2B e Compras

* Catálogo de distribuidores integrados.
* Sistema de solicitação de compras entre lojistas e fornecedores.
* Atualização de status de pedidos (Aguardando liberação, Em rota, Entregue).
* Integração de endereços via API **ViaCEP**.

### 📦 Gestão de Estoque e Produtos

* Controle rigoroso de estoque com baixa e crédito automático de itens.
* Códigos gerados dinamicamente para o catálogo de produtos.
* Upload de imagens de produtos com armazenamento e validação de formato.

### 🚚 Logística e Rastreio

* Cadastro de caminhoneiros e rotas.
* Linha do tempo de entregas (Separação, Carregamento, Em trânsito, Entregue).
* Cálculo de status dinâmico e rastreio de motoristas.

### 📊 Dashboard Financeiro

* Resumo mensal de faturamento, custos e margem de lucro.
* Controle de movimentação diária de estoque (StockDia).
* Gerenciamento de métodos de pagamento salvos (PIX, Cartões de Crédito/Débito mascarados).

### 🎪 PDV e Eventos

* Criação de eventos e alocação de "Barracas/Quiosques".
* Transferência de estoque do depósito principal para as barracas.
* Registro de pedidos de ponto de venda (PDV).

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java 17
* **Framework:** Spring Boot 3.4.0
* **Banco de Dados:** PostgreSQL
* **Segurança:** Spring Security + JWT (JJWT)
* **ORM:** Spring Data JPA / Hibernate
* **Documentação:** SpringDoc OpenAPI (Swagger UI)
* **Ferramentas:** Maven, Lombok

## 🚀 Como Executar o Projeto Localmente

### Pré-requisitos

* Java 17 ou superior
* PostgreSQL 14 ou superior
* Maven 3.9+ (ou use o *wrapper* incluso)

### Passo a Passo

1. **Clone o repositório:**

   ```bash
   git clone https://github.com/seu-usuario/stock4event-backend.git
   cd stock4event-backend
   ```

2. **Configure o Banco de Dados:**
   Crie um banco de dados no PostgreSQL chamado `quickstock` (nome configurado atualmente no arquivo `application.properties`). O sistema criará as tabelas automaticamente.

   ```sql
   CREATE DATABASE quickstock;
   ```

3. **Configure as credenciais (Opcional):**
   Edite o arquivo `src/main/resources/application.properties` se o seu usuário/senha do Postgres for diferente de `postgres`/`admin`.

4. **Execute a aplicação:**

   * **No Windows:**

     ```bash
     mvnw.cmd spring-boot:run
     ```

   * **No Linux/Mac:**

     ```bash
     ./mvnw spring-boot:run
     ```

A API estará rodando em: `http://localhost:8081`

## 🧪 Dados de Demonstração (Seeders)

Ao iniciar a aplicação pela primeira vez, o sistema executa scripts automáticos (`DemoSeedRunner`, `MarketplaceDataFixRunner`, etc.) que populam o banco com dados de teste.

Você pode acessar o sistema imediatamente usando a conta de demonstração configurada no código:

* **E-mail:** `demo@quickstock.com`
* **Senha:** `demo1234`
* **Perfil:** Cliente e Fornecedor (Acesso completo às abas)

*A base também será populada com distribuidores parceiros (Casa dos Vinhos, Cervejaria Caruaru, Whisky Labs) e catálogos de produtos prontos para teste.*

## 📚 Documentação da API (Swagger)

A documentação interativa de todos os endpoints (rotas, parâmetros e DTOs) está disponível via Swagger UI.

Com a aplicação rodando, acesse:
👉 [**http://localhost:8081/swagger**](http://localhost:8081/swagger)

## 🏗️ Estrutura do Projeto

O projeto segue a arquitetura padrão em camadas (N-Tier):

```text
src/main/java/com/quickstock/backend/
 ├── config/       # Configurações gerais (CORS, Seeders do BD, WebConfig)
 ├── controller/   # Endpoints da API REST
 ├── dto/          # Objetos de Transferência de Dados (Requests/Responses)
 ├── entity/       # Modelos de domínio mapeados para o banco (JPA)
 ├── exception/    # Tratamento global de erros (Controller Advice)
 ├── repository/   # Interfaces de acesso a dados (Spring Data JPA)
 └── service/      # Regras de negócio e lógica principal
```

## 🤝 Equipe de Desenvolvimento

* Luan Feitosa Santos
* José Ítalo S. C. Dantas
* Marcelo Vitor Viana da Silva
* Leticia Viviane Pereira da Silva

## 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos e de aprendizado.
