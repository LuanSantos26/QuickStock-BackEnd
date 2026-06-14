📦 QuickStock - Back-end
O QuickStock Back-end é uma API RESTful desenvolvida em Java com Spring Boot. Este sistema fornece toda a infraestrutura necessária para a gestão de inventário, controlo de vendas, gestão de eventos (com alocação de barracas), processamento financeiro e autenticação de utilizadores.

🚀 Tecnologias Utilizadas
O projeto foi construído utilizando as seguintes tecnologias e bibliotecas:

Java 17 - Linguagem principal.

Spring Boot 3.4.0 - Framework base para a aplicação web.

Spring Data JPA / Hibernate - Mapeamento objeto-relacional (ORM) e persistência de dados.

PostgreSQL - Base de dados relacional principal.

Lombok - Redução de código repetitivo (Getters, Setters, Construtores).

JJWT (JSON Web Token) - Autenticação e autorização seguras (implementação customizada sem bloqueios estritos do Spring Security).

Springdoc OpenAPI (Swagger) - Documentação automática e interativa da API.

Maven - Gestão de dependências e automação de build.

⚙️ Funcionalidades Principais
Gestão de Utilizadores e Perfis: Registo, login com JWT e controlo de acessos (Administradores, Vendedores, etc.).

Gestão de Stock e Produtos: Criação de produtos, controlo de quantidades, catálogos e gestão de múltiplos fornecedores/marketplaces.

Gestão de Eventos e Barracas: Organização de eventos, alocação de stock físico para barracas específicas e acompanhamento de vendas em tempo real.

Processamento de Pedidos: Carrinho de compras, histórico de pedidos e alteração de estados de envio/entrega.

Módulo Financeiro: Registo de pagamentos, gestão de cartões/formas de pagamento guardadas e visualização de resumos financeiros mensais (lucros, valores e pedidos).

Upload de Ficheiros: Sistema integrado para upload e fornecimento estático de imagens de produtos e logótipos de empresas.

📋 Pré-requisitos
Para executar este projeto localmente, precisará de ter instalado no seu ambiente:

Java Development Kit (JDK) 17 ou superior.

Apache Maven (opcional, o projeto inclui o Maven Wrapper mvnw).

PostgreSQL (em execução na porta 5432).

🛠️ Configuração do Ambiente
Clone o repositório:

Bash
git clone https://github.com/luansantos26/quickstock-backend.git
cd quickstock-backend
Configure a Base de Dados:
Abra o ficheiro src/main/resources/application.properties e ajuste as credenciais do PostgreSQL, caso sejam diferentes do padrão:

Properties
spring.datasource.url=jdbc:postgresql://localhost:5432/quickstock
spring.datasource.username=postgres
spring.datasource.password=admin # Altere para a sua palavra-passe
Configuração de Diretórios de Imagens:
O projeto guarda e serve imagens localmente. Certifique-se de que a aplicação tem permissões de escrita na diretoria definida em upload.dir no ficheiro application.properties (por defeito: uploads/produtos).

▶️ Execução do Projeto
Pode executar a aplicação diretamente através do terminal utilizando o Maven Wrapper incluído no projeto:

Em Windows:

DOS
mvnw.cmd spring-boot:run
Em Linux/macOS:

Bash
./mvnw spring-boot:run
A API ficará disponível no endereço: http://localhost:8080.

(Nota: Na primeira execução, o Hibernate irá criar automaticamente as tabelas na base de dados, e os ficheiros SeedRunner irão popular os dados iniciais necessários, como formas de pagamento e endereços padrão).

📚 Documentação da API (Swagger)
A API está totalmente documentada através do Swagger/OpenAPI. Uma vez que o servidor esteja em execução, pode aceder à interface gráfica interativa para explorar e testar os endpoints.

Interface Gráfica (Atalho configurado): http://localhost:8080/swagger

Interface Gráfica (Caminho completo): http://localhost:8080/swagger-ui/index.html

Especificação JSON (OpenAPI 3): http://localhost:8080/v3/api-docs

📁 Estrutura do Código-Fonte
O projeto segue a arquitetura em camadas padrão do Spring Boot:

config/: Classes de configuração global (CORS, mapeamento de recursos estáticos do WebConfig, Seeds de base de dados).

controller/: Endpoints REST que recebem as requisições HTTP e devolvem as respostas.

dto/: Data Transfer Objects utilizados para mapear dados de entrada (Request) e saída (Response), mantendo as entidades protegidas.

entity/: Modelos de domínio que representam as tabelas na base de dados PostgreSQL.

exception/: Tratamento global de erros e exceções customizadas (GlobalExceptionHandler).

repository/: Interfaces do Spring Data JPA para comunicação direta com a base de dados.

service/: Camada de regras de negócio, processamento de lógica e validações estruturais.

🔒 Segurança (JWT)
Este projeto utiliza JSON Web Tokens (JWT) para garantir que as operações sensíveis são feitas de forma segura.
A chave secreta e o tempo de expiração do token podem ser ajustados no ficheiro application.properties:

Properties
jwt.secret=SuaChaveSecretaSuperSeguraAqui
jwt.expiration-ms=86400000 # Duração em milissegundos (ex: 24 horas)
A segurança das rotas (validação de tokens) é gerida manualmente nos serviços/controladores relevantes, proporcionando flexibilidade na documentação e no acesso público a rotas específicas.

👨‍💻 Autores
Desenvolvido por Luan Feitosa Santos, José Italo S C Dantas, Marcelo Vitor Viana da Silva, Leticia Viviane Pereira da Silva.
