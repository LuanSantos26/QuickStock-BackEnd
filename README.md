# 🚀 Stock4event API

Backend do sistema **Stock4event**, uma plataforma completa para gerenciamento de estoque, vendas, eventos e controle financeiro.

A API foi desenvolvida utilizando **Java 17**, **Spring Boot** e **PostgreSQL**, seguindo uma arquitetura escalável baseada em camadas, preparada para futuras expansões e integração com aplicações web, mobile e sistemas de terceiros.

---

## 📖 Sobre o Projeto

O Stock4event foi criado para centralizar a gestão operacional de empresas que necessitam controlar:

* Estoque de produtos
* Vendas e pedidos
* Clientes e fornecedores
* Eventos e barracas
* Fluxo financeiro
* Upload de arquivos e imagens
* Controle de acesso por perfil de usuário

O sistema foi projetado para ser modular, permitindo a inclusão de novos recursos sem impactar a estrutura principal da aplicação.

---

## 🛠️ Tecnologias Utilizadas

### Backend

* Java 17
* Spring Boot 3.4
* Spring Data JPA
* Hibernate ORM
* Maven
* Lombok

### Banco de Dados

* PostgreSQL

### Segurança

* JWT (JSON Web Token)

### Documentação

* Swagger / OpenAPI 3
* SpringDoc OpenAPI

---

## ✨ Principais Funcionalidades

### 👥 Gestão de Usuários

* Cadastro de usuários
* Login com autenticação JWT
* Controle de permissões
* Perfis de acesso

### 📦 Gestão de Estoque

* Cadastro de produtos
* Controle de quantidade
* Atualização automática de estoque
* Gestão de fornecedores

### 🛒 Gestão de Vendas

* Registro de pedidos
* Histórico de vendas
* Controle de status
* Integração com estoque

### 🎪 Gestão de Eventos

* Cadastro de eventos
* Organização de barracas
* Distribuição de estoque por evento
* Monitoramento de vendas em tempo real

### 💰 Módulo Financeiro

* Registro de pagamentos
* Controle de receitas
* Relatórios financeiros
* Resumo mensal de faturamento

### 🖼️ Upload de Arquivos

* Upload de imagens de produtos
* Upload de logotipos
* Armazenamento local configurável

---

## 🏗️ Arquitetura do Projeto

A aplicação segue o padrão de arquitetura em camadas:

```text
src/main/java/com/stock4event
│
├── config
├── controller
├── dto
├── entity
├── exception
├── repository
├── security
├── service
└── util
```

### Descrição das Camadas

| Camada     | Responsabilidade           |
| ---------- | -------------------------- |
| controller | Endpoints REST             |
| service    | Regras de negócio          |
| repository | Acesso ao banco            |
| entity     | Modelos persistidos        |
| dto        | Objetos de transferência   |
| config     | Configurações globais      |
| exception  | Tratamento de erros        |
| security   | Autenticação e autorização |

---

## 📋 Pré-requisitos

Antes de executar o projeto, certifique-se de possuir:

* Java 17+
* PostgreSQL 14+
* Maven 3.9+
* Git

---

## ⚙️ Configuração Local

### 1. Clonar o repositório

```bash
git clone https://github.com/luansantos26/quickstock-backend.git

cd quickstock-backend
```

### 2. Criar o banco de dados

```sql
CREATE DATABASE stock4event;
```

Com o banco vazio, o backend cria as tabelas e os dados iniciais sozinho ao subir. Se preferir montar o banco antes, ou restaurar o banco oficial, use os scripts da pasta [`database/`](database/GUIA_DE_USO.md):

```bash
export PGPASSWORD=sua_senha
cd database
./build_from_scratch.sh                         # recria do zero (schema + catálogo + conta demo)
./restore_stock4event.sh stock4event_full.sql   # ou restaura o dump oficial
```

A pasta também tem o diagrama ER (`stock4event_er.png`), o script de backup e o guia completo de uso do banco.

### 3. Configurar o application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/stock4event
spring.datasource.username=postgres
spring.datasource.password=sua_senha

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 4. (Opcional) E-mail de boas-vindas

Após o cadastro, o backend pode enviar um e-mail de boas-vindas (versões cliente e fornecedor). O envio vem
desligado; para ligar, preencha as variáveis de ambiente do modelo [`email.env.example`](email.env.example).
Configuração do provedor, teste local com Mailpit e produção: [`docs/EMAIL_BOAS_VINDAS.md`](docs/EMAIL_BOAS_VINDAS.md).

---

## ▶️ Executando a Aplicação

### Windows

```bash
mvnw.cmd spring-boot:run
```

### Linux / macOS

```bash
./mvnw spring-boot:run
```

A aplicação ficará disponível em:

```text
http://localhost:8081
```

### Conta de demonstração

Ao iniciar, o `DemoSeedRunner` cria (se ainda não existir) uma conta administradora para a apresentação, com acesso às telas de cliente e de fornecedor, já com endereço, cartão e chave PIX:

* E-mail: `demo@stock4event.com`
* Senha: `demo1234`
* Empresa: Mercado Demo (perfil Cliente e fornecedor)

---

## 📚 Documentação da API

Após iniciar a aplicação:

### Swagger UI

```text
http://localhost:8081/swagger
```

ou

```text
http://localhost:8081/swagger-ui/index.html
```

### OpenAPI JSON

```text
http://localhost:8081/v3/api-docs
```

---

## 🔒 Segurança

A autenticação utiliza JWT (JSON Web Token).

Configuração padrão:

```properties
jwt.secret=SUA_CHAVE_SECRETA
jwt.expiration-ms=86400000
```

### Fluxo de autenticação

1. Usuário realiza login
2. API gera token JWT
3. Cliente envia token no header Authorization
4. API valida o token e autoriza o acesso

### Regras de acesso (Spring Security)

* **Públicas:** `POST /api/usuarios/login`, `POST /api/cadastro`, `POST /api/usuarios/recuperar-senha`, `GET /uploads/**` e o Swagger quando `stock4event.seguranca.swagger-publico=true`.
* **Todo o resto exige** `Authorization: Bearer <token>`, com assinatura e expiração conferidas e usuário ativo. Sem token válido: **401**.
* **A empresa vem do token, não do app.** Se um `empresaId` (ou `empresaCompradoraId`/`empresaFornecedoraId`) diferente da empresa do usuário for enviado em parâmetro ou corpo, a resposta é **403**. Sem o parâmetro, vale a empresa do token.
* Registros de outra empresa (consultar, alterar, excluir) respondem **403** e o banco não muda.
* Campos controlados pelo servidor (empresa dona, perfil, status ativo, operador, tipo e total de pedido, status inicial de pagamento) não são aceitos do cliente.

### Testes automatizados

Os testes usam um banco **exclusivo**, `stock4event_test`, criado automaticamente e recriado a cada execução (os testes recusam qualquer banco que não termine em `_test`).

```bash
mvnw.cmd test        # Windows
./mvnw test          # Linux / macOS
```

Variáveis opcionais: `TEST_DB_HOST`, `TEST_DB_PORT`, `TEST_DB_NAME`, `TEST_DB_USER`, `TEST_DB_PASSWORD` (padrão: localhost, 5432, stock4event_test, postgres, admin).

---

## 📈 Roadmap

Funcionalidades planejadas para futuras versões:

* Dashboard analítico avançado
* Relatórios em PDF
* Integração com PIX
* Integração com Mercado Pago
* Controle de múltiplas filiais
* Sistema de notificações
* Auditoria de operações
* Logs centralizados
* Deploy em Docker
* Integração CI/CD

---

## 🤝 Contribuição

Contribuições são bem-vindas.

1. Faça um fork do projeto
2. Crie uma branch para sua feature

```bash
git checkout -b feature/minha-feature
```

3. Commit suas alterações

```bash
git commit -m "feat: adiciona nova funcionalidade"
```

4. Envie para seu fork

```bash
git push origin feature/minha-feature
```

5. Abra um Pull Request

---

## 👨‍💻 Equipe

* Luan Feitosa Santos
* José Ítalo S. C. Dantas
* Marcelo Vitor Viana da Silva
* Leticia Viviane Pereira da Silva

---

## 📄 Licença

Este projeto é destinado a fins acadêmicos e de aprendizado, podendo ser expandido para utilização comercial mediante adequações futuras.
