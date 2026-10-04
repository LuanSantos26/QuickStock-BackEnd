# 🖥️ QuickStock - Back-End API

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)

O **QuickStock Back-End** é a API RESTful central do ecossistema QuickStock. Desenvolvida em **Java** com o framework **Spring Boot**, a aplicação foi projetada para prover persistência robusta, regras de negócio e comunicação escalável para gestão de inventário descentralizado, operações de quiosques/barracas em eventos e integração com distribuidores de mercadorias.

Esta API atende diretamente o [QuickStock Mobile App](https://github.com/luansantos26/quickstock-mobile).

---

## 🚀 Funcionalidades

A API provê suporte à infraestrutura operacional e transacional do sistema, contemplando fluxos de:

* **🔒 Autenticação e Perfis:** Cadastro de contas, gerenciamento de credenciais e controle de níveis de acesso por perfil de usuário e vínculo corporativo.
* **🏢 Gestão de Empresas e Vínculos:** Registro de distribuidores e compradores parceiros, suportando modelos de catálogo integrado.
* **📦 Gestão de Produtos e Estoques:** Controle de inventário consolidado e fracionado por pontos de venda (barracas/quiosques).
* **🎪 Operações de Eventos e Barracas:** Estruturação de eventos sazonais e alocação de estoque por operador de campo.
* **🧾 Pedidos e Distribuição:** Criação, cálculo e acompanhamento de ordens de compra e reposição de itens.
* **💳 Gestão de Pagamentos e Endereços:** Registro de dados de entrega e faturamento para liquidação das transações.

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem Principal:** Java 17+
* **Framework Principal:** Spring Boot 3
* **Acesso a Dados e ORM:** Spring Data JPA / Hibernate
* **Banco de Dados:** PostgreSQL (Produção/Desenvolvimento) e H2 Database (Testes locais)
* **Gerenciador de Dependências e Build:** Apache Maven
* **Padrão Arquitetural:** RESTful API em arquitetura em camadas (Controller, Service, Repository, Entity)

---

## 📁 Estrutura do Projeto

O código-fonte segue o padrão arquitetural em camadas do ecossistema Spring:

```text
📦 QuickStock-BackEnd
 ┣ 📂 .mvn/wrapper/                  # Configurações do Maven Wrapper
 ┣ 📂 src/
 ┃ ┣ 📂 main/
 ┃ ┃ ┣ 📂 java/Projeto/QuickStock/
 ┃ ┃ ┃ ┣ 📂 Controller/              # Endpoints e recepção de requisições HTTP
 ┃ ┃ ┃ ┃ ┗ 📜 cadastroController.java
 ┃ ┃ ┃ ┣ 📂 Entity/                  # Mapeamento Objeto-Relacional (JPA Entities)
 ┃ ┃ ┃ ┃ ┣ 📜 Cadastro.java
 ┃ ┃ ┃ ┃ ┗ 📜 Escolhar.java
 ┃ ┃ ┃ ┣ 📂 Repository/              # Interfaces de persistência e consultas (Spring Data)
 ┃ ┃ ┃ ┃ ┗ 📜 CadastroRepository.java
 ┃ ┃ ┃ ┣ 📂 Server/                  # Regras de negócio e processamento de dados (Services)
 ┃ ┃ ┃ ┃ ┗ 📜 CadastroServer.java
 ┃ ┃ ┃ ┗ 📜 QuickStockApplication.java # Classe de inicialização da aplicação Spring Boot
 ┃ ┃ ┗ 📂 resources/
 ┃ ┃   ┗ 📜 application.properties   # Configurações da aplicação, porta e conexão com banco
 ┃ ┗ 📂 test/
 ┃   ┗ 📂 java/Projeto/QuickStock/
 ┃     ┗ 📜 QuickStockApplicationTests.java # Testes unitários e de integração
 ┣ 📜 .gitattributes
 ┣ 📜 .gitignore
 ┣ 📜 mvnw                           # Wrapper de execução do Maven para Linux/macOS
 ┣ 📜 mvnw.cmd                       # Wrapper de execução do Maven para Windows
 ┗ 📜 pom.xml                        # Definições de dependências e plugins Maven
```

---

## ⚙️ Pré-requisitos

Antes de iniciar, certifique-se de ter instalado em seu ambiente:

* **Java JDK 17** ou superior instalado e configurado nas variáveis de ambiente (`JAVA_HOME`).
* **PostgreSQL** instalado e ativo (ou uma instância rodando via Docker).
* **Git** instalado.
* **Postman**, **Insomnia** ou extensão HTTP para testes dos endpoints.

> **Aviso:** Certifique-se de que o QuickStock Backend esteja rodando na porta configurada (padrão: `8080`) e com o banco de dados inicializado para permitir o consumo correto das requisições pelo aplicativo mobile.

---

## 🚀 Como Executar o Projeto

### 1. Clone o repositório
```bash
git clone [https://github.com/luansantos26/quickstock-backend.git](https://github.com/luansantos26/quickstock-backend.git)
cd quickstock-backend
```

### 2. Configure o Banco de Dados
Acesse o arquivo `src/main/resources/application.properties` e insira as credenciais de conexão do seu banco local:

```properties
spring.application.name=QuickStock
server.port=8080

# Configuração PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/quickstock
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.datasource.driver-class-name=org.postgresql.Driver

# Configurações do Hibernate / JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### 3. Compile e execute a aplicação

* **Linux / macOS:**
  ```bash
  ./mvnw spring-boot:run
  ```

* **Windows (CMD ou PowerShell):**
  ```cmd
  mvnw.cmd spring-boot:run
  ```

A aplicação inicializará no endereço base: `http://localhost:8080`

---

## 🔌 Principais Endpoints da API

### Módulo de Cadastros (`/cadastro`)

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/cadastro` | Cria um novo registro de usuário ou perfil |
| `GET` | `/cadastro` | Lista todos os cadastros registrados |
| `GET` | `/cadastro/{id}` | Recupera os dados de um cadastro por ID |
| `PUT` | `/cadastro/{id}` | Atualiza as informações de um cadastro existente |
| `DELETE` | `/cadastro/{id}` | Remove um cadastro do sistema |

#### Exemplo de Payload de Envio (`POST /cadastro`):
```json
{
  "nome": "Operador Barraca Centro",
  "email": "operador@quickstock.com",
  "senha": "senhaSegura123",
  "tipoPerfil": "VENDEDOR"
}
```

---

## 📑 Scripts e Comandos Úteis

No diretório raiz do projeto, você pode executar:

* `./mvnw clean install` (ou `mvnw.cmd clean install`): Baixa dependências, executa testes e compila o pacote final `.jar`.
* `./mvnw test` (ou `mvnw.cmd test`): Executa a suíte de testes automatizados do projeto.
* `./mvnw spring-boot:run` (ou `mvnw.cmd spring-boot:run`): Roda o servidor de desenvolvimento em tempo de execução.

---

## 💡 Notas de Desenvolvimento

* **Conexão com Front-End Mobile:** Ao testar com o emulador ou celular físico via Expo, utilize o endereço IP da máquina host local na rede Wi-Fi (ex: `http://192.168.0.xxx:8080`) no arquivo de configuração de API do app móvel em vez de `localhost`.
* **CORS (Cross-Origin Resource Sharing):** Certifique-se de configurar as anotações `@CrossOrigin` nos controladores para permitir a integração sem bloqueios com o cliente mobile.
* **Modelo de Dados:** A evolução das tabelas reflete o dicionário de dados estabelecido para suportar múltiplos perfis, catálogos de fornecedores e controle de fluxo de caixa por barraca.

---

## 👨‍💻 Equipe

* **Luan Feitosa Santos**
* **José Ítalo S. C. Dantas**
* **Marcelo Vitor Viana da Silva**
* **Leticia Viviane Pereira da Silva**
* **José Lucas Luiz da Silva**

---

## 📄 Licença

Este projeto é destinado a fins acadêmicos e de aprendizado, podendo ser expandido para utilização comercial mediante adequações futuras.
