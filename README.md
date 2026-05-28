# 📦 QuickStock - Back-End API

O **QuickStock** é um sistema robusto de back-end projetado para gerenciar operações de estoque, vendas e controle financeiro em eventos. Desenvolvida com foco em escalabilidade e organização de dados, esta API RESTful atende às necessidades operacionais de empresas que gerenciam múltiplos pontos de venda simultaneamente.

---

## 🚀 Funcionalidades Principais

A arquitetura do sistema engloba as seguintes áreas de negócio[cite: 1]:

*   **Gestão de Entidades Base:** Cadastro, controle e relacionamento entre `Empresa`, `Evento` e `Perfil` de acesso[cite: 1].
*   **Controle de Estoque Setorizado:** Gerenciamento centralizado de `Produto` e `Barraca` (pontos de venda), com controle específico e independente de `EstoqueBarraca`[cite: 1].
*   **Fluxo de Caixa (PDV):** Processamento completo do ciclo de vendas, incluindo a criação de um `Pedido`, a adição de múltiplos `ItemPedido` e a conciliação do `Pagamento`[cite: 1].
*   **Segurança e Usuários:** Controle de acesso através da entidade `Usuario`, utilizando objetos de transferência de dados (como o `UsuarioResponseDTO`) para garantir que dados sensíveis não sejam expostos nas respostas da API[cite: 1].

---

## 🛠️ Tecnologias Utilizadas

A aplicação foi construída com tecnologias modernas e consolidadas do ecossistema corporativo[cite: 1]:

*   **Linguagem:** Java[cite: 1]
*   **Framework Principal:** Spring Boot (`BackendApplication.java`)[cite: 1]
*   **Gerenciamento de Dependências:** Maven (utilizando o wrapper nativo `mvnw` e `pom.xml`)[cite: 1]
*   **Persistência de Dados:** Spring Data JPA / Hibernate (configurável via `application.properties`)[cite: 1]

---

## 📂 Arquitetura do Projeto

O código-fonte segue o padrão arquitetural em camadas, facilitando a manutenção e a injeção de dependências[cite: 1]:

*   `controller/`: Camada de exposição dos endpoints da API REST (ex: `ProdutoController`, `PedidoController`)[cite: 1].
*   `entity/`: Modelagem de domínio e mapeamento objeto-relacional (ORM) das tabelas do banco de dados[cite: 1].
*   `repository/`: Interfaces do Spring Data para abstração do acesso e manipulação dos dados (ex: `PagamentoRepository`, `EventoRepository`)[cite: 1].
*   `dto/`: Contratos de entrada e saída (Data Transfer Objects) para otimização e segurança das requisições[cite: 1].

---

## ⚙️ Como Executar Localmente

### Pré-requisitos
*   **Java Development Kit (JDK):** Versão 17 ou superior instalada.
*   **Banco de Dados:** SGBD relacional configurado e rodando localmente.

### Passos para Instalação

1. **Clone o repositório** para a sua máquina:
```bash
   git clone [https://github.com/luansantos26/quickstock-backend.git](https://github.com/luansantos26/quickstock-backend.git)
