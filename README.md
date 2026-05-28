# 📦 QuickStock - Back-End

Bem-vindo ao repositório do **QuickStock**, um sistema back-end inteligente desenvolvido para o gerenciamento de estoque e vendas em eventos. A aplicação permite que empresas gerenciem múltiplos eventos, barracas, produtos, usuários (operadores) e o fluxo completo de pedidos e pagamentos.

A API foi construída em **Java com Spring Boot**, fornecendo endpoints RESTful robustos, com relacionamentos de dados bem definidos e hash de senhas para segurança básica.

## 🚀 Tecnologias Utilizadas

* **Java 17**: Linguagem de programação.
* **Spring Boot 3.4.0**: Framework principal para construção da API REST.
* **Spring Data JPA & Hibernate**: ORM para persistência e mapeamento de dados.
* **PostgreSQL**: Banco de dados relacional principal.
* **Spring Security Crypto (BCrypt)**: Para criptografia de senhas de usuários.
* **Lombok**: Redução de código boilerplate (getters, setters, construtores).
* **Maven**: Gerenciador de dependências.

## ⚙️ Arquitetura e Domínio

O sistema é focado na gestão de vendas em eventos, possuindo as seguintes entidades principais:
- **Empresa & Perfil**: Entidades raiz para controle de acesso corporativo.
- **Usuário**: Operadores do sistema (com senhas criptografadas).
- **Produto & Evento**: Cadastros base do negócio.
- **Barraca & EstoqueBarraca**: Controle de pontos de venda dentro de um evento e seus respectivos estoques.
- **Pedido, ItemPedido & Pagamento**: Fluxo completo de caixa (carrinho de compras, cálculo de subtotais e formas de pagamento).

## 🛠️ Como Executar o Projeto

### Pré-requisitos
* JDK 17 instalado.
* Maven instalado (ou use o `./mvnw` incluso no projeto).
* PostgreSQL rodando localmente na porta `5432`.

### Configuração do Banco de Dados
1. Crie um banco de dados no PostgreSQL chamado `quickstock`.
2. Certifique-se de que o usuário e senha no seu arquivo `application.properties` correspondam ao seu ambiente local:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/quickstock
   spring.datasource.username=postgres
   spring.datasource.password=123456
