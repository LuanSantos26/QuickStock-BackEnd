# 📦 Stock4Event - Backend

[![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)

O **Stock4Event Backend** é a API REST que alimenta o ecossistema Stock4Event. Desenvolvido com **Java 17** e **Spring Boot**, este sistema foi projetado para centralizar e otimizar a gestão operacional de estoques, vendas, eventos e controle financeiro de forma escalável e segura.

Este repositório fornece a inteligência e persistência de dados consumidas pelo [Aplicativo Mobile Stock4Event](https://github.com/marcelo435/Mobile).

---

## 🎯 Sobre o Projeto

O Stock4Event foi criado para centralizar a gestão operacional de empresas que necessitam controlar:

* Estoque de produtos
* Vendas e pedidos
* Clientes e fornecedores
* Eventos e barracas
* Fluxo financeiro
* Upload de arquivos e imagens
* Controle de acesso por perfil de usuário

O sistema foi projetado para ser modular, seguindo uma arquitetura baseada em camadas, permitindo a inclusão de novos recursos sem impactar a estrutura principal da aplicação e facilitando integrações futuras.

---

## 🚀 Principais Funcionalidades

* **🔒 Gestão de Usuários e Segurança:**
  * Cadastro de usuários com perfis de acesso distintos.
  * Autenticação segura via JWT (JSON Web Token).
  * Controle rigoroso de permissões e rotas.
* **📦 Gestão de Estoque:**
  * Cadastro detalhado de produtos.
  * Controle de quantidade e atualização automática de saldo.
  * Gestão integrada de fornecedores.
* **🛍️ Gestão de Vendas:**
  * Registro completo de pedidos.
  * Histórico detalhado de vendas.
  * Controle de status de pedidos (com avanço automático para demonstrações).
  * Integração direta com a baixa de estoque.
* **🎪 Gestão de Eventos:**
  * Cadastro e organização de eventos e barracas.
  * Distribuição inteligente de estoque por evento.
  * Monitoramento de vendas em tempo real.
* **💰 Módulo Financeiro:**
  * Registro e acompanhamento de pagamentos.
  * Controle de receitas e emissão de relatórios financeiros.
  * Resumo mensal de faturamento.
* **📁 Upload de Arquivos:**
  * Upload de imagens para produtos e logotipos de empresas.
  * Armazenamento local configurado de forma estática.

---

## 🛠️ Tecnologias Utilizadas

**Backend & Frameworks:**
* Java 17
* Spring Boot 3.4
* Spring Data JPA
* Hibernate ORM
* Maven
* Lombok

**Banco de Dados:**
* PostgreSQL

**Segurança:**
* Spring Security
* JWT (JSON Web Token)
* BCrypt (Hash de senhas)

**Documentação:**
* Swagger / OpenAPI 3
* SpringDoc OpenAPI

---

## 📁 Estrutura do Projeto (Arquitetura em Camadas)

A aplicação segue o padrão de arquitetura em camadas para garantir separação de responsabilidades e manutenibilidade:

```text
src/main/java/com/quickstock
 ┣ 📂 config      # Configurações globais (Segurança, Swagger, CORS)
 ┣ 📂 controller  # Endpoints REST (Controladores)
 ┣ 📂 dto         # Objetos de Transferência de Dados (Requisições/Respostas)
 ┣ 📂 entity      # Modelos de Domínio persistidos no banco
 ┣ 📂 exception   # Tratamento centralizado de erros e exceções
 ┣ 📂 repository  # Interfaces de acesso a dados (Spring Data JPA)
 ┣ 📂 security    # Filtros e lógicas de autenticação/autorização (JWT)
 ┣ 📂 service     # Regras de negócio da aplicação
 ┗ 📂 util        # Classes utilitárias diversas
