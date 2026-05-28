# 📦 QuickStock - Back-End API

O **QuickStock** é um sistema robusto de back-end projetado para gerenciar operações de estoque, vendas e controle financeiro em eventos. Desenvolvida com foco em escalabilidade e organização de dados, esta API RESTful atende às necessidades operacionais de empresas que gerenciam múltiplos pontos de venda simultaneamente.

---

## 🚀 Funcionalidades Principais

A arquitetura do sistema engloba as seguintes áreas de negócio:

* **Gestão de Entidades Base:** Cadastro, controle e relacionamento entre `Empresa`, `Evento` e `Perfil` de acesso.
* **Controle de Estoque Setorizado:** Gerenciamento centralizado de `Produto` e `Barraca` (pontos de venda), com controle específico e independente de `EstoqueBarraca`.
* **Fluxo de Caixa (PDV):** Processamento completo do ciclo de vendas, incluindo a criação de um `Pedido`, a adição de múltiplos `ItemPedido` e a conciliação do `Pagamento`.
* **Segurança e Usuários:** Controle de acesso através da entidade `Usuario`, utilizando objetos de transferência de dados (como o `UsuarioResponseDTO`) para garantir que dados sensíveis não sejam expostos nas respostas da API.

---

## 🛠️ Tecnologias Utilizadas

A aplicação foi construída com tecnologias modernas e consolidadas do ecossistema corporativo:

* **Linguagem:** Java
* **Framework Principal:** Spring Boot (`BackendApplication.java`)
* **Gerenciamento de Dependências:** Maven (utilizando o wrapper nativo `mvnw` e `pom.xml`)
* **Persistência de Dados:** Spring Data JPA / Hibernate (configurável via `application.properties`)

---

## 📂 Arquitetura do Projeto

O código-fonte segue o padrão arquitetural em camadas, facilitando a manutenção e a injeção de dependências:

* `controller/`: Camada de exposição dos endpoints da API REST (ex: `ProdutoController`, `PedidoController`).
* `entity/`: Modelagem de domínio e mapeamento objeto-relacional (ORM) das tabelas do banco de dados.
* `repository/`: Interfaces do Spring Data para abstração do acesso e manipulação dos dados (ex: `PagamentoRepository`, `EventoRepository`).
* `dto/`: Contratos de entrada e saída (Data Transfer Objects) para otimização e segurança das requisições.

---

## ⚙️ Como Executar Localmente

### Pré-requisitos
* **Java Development Kit (JDK):** Versão 17 ou superior instalada.
* **Banco de Dados:** PostgreSQL configurado e rodando localmente na porta `5432`.

### Passos para Instalação

1. **Clone o repositório** para a sua máquina:
   ```bash
   git clone [https://github.com/luansantos26/quickstock-backend.git](https://github.com/luansantos26/quickstock-backend.git)
   ```

2. **Navegue até a raiz do projeto:**
   ```bash
   cd quickstock-backend
   ```

3. **Configure as variáveis de ambiente:**
   Acesse o arquivo `src/main/resources/application.properties` e verifique as credenciais (URL, usuário e senha) do seu banco de dados local. Elas devem bater com as do seu PostgreSQL:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/quickstock
   spring.datasource.username=postgres
   spring.datasource.password=123456
   ```

4. **Compile e execute a aplicação:**
   Utilize o Maven Wrapper incluso no projeto para baixar as dependências e iniciar o servidor.

   * **No Linux / macOS:**
     ```bash
     ./mvnw spring-boot:run
     ```
   * **No Windows:**
     ```cmd
     mvnw.cmd spring-boot:run
     ```

A API será inicializada e estará pronta para receber requisições HTTP na porta configurada (padrão: `8080`).

---

## 👨‍💻 Autores

Desenvolvido por **Luan Feitosa Santos**  **José Italo S C Dantas**  **Marcelo Vitor Viana da Silva**  **Leticia Viviane Pereira da Silva**.
