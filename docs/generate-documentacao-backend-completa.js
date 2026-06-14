const fs = require('fs');
const path = require('path');
const PDFDocument = require('pdfkit');

const outputPath = path.join(__dirname, 'documentacao-backend-quickstock-completa.pdf');

const doc = new PDFDocument({
  size: 'A4',
  margins: { top: 50, bottom: 50, left: 50, right: 50 },
  info: {
    Title: 'QuickStock BackEnd - Documentação Completa',
    Author: 'QuickStock',
    Subject: 'Modelo conceitual, lógico e físico + arquitetura da API',
    CreationDate: new Date(),
  },
});

doc.pipe(fs.createWriteStream(outputPath));

const C = {
  primary: '#1a365d',
  secondary: '#2c5282',
  accent: '#2b6cb0',
  text: '#1a202c',
  muted: '#4a5568',
  line: '#cbd5e0',
  mono: '#2d3748',
};

function ensure(h = 70) {
  if (doc.y > doc.page.height - h) doc.addPage();
}

function hr() {
  doc.moveDown(0.25);
  doc.strokeColor(C.line).lineWidth(0.5)
    .moveTo(doc.page.margins.left, doc.y)
    .lineTo(doc.page.width - doc.page.margins.right, doc.y)
    .stroke();
  doc.moveDown(0.45);
}

function section(t) {
  ensure(90);
  doc.moveDown(0.5);
  doc.font('Helvetica-Bold').fontSize(13).fillColor(C.primary).text(t);
  doc.moveDown(0.15);
  hr();
}

function sub(t) {
  ensure(55);
  doc.font('Helvetica-Bold').fontSize(10.5).fillColor(C.secondary).text(t);
  doc.moveDown(0.12);
}

function p(t, o = {}) {
  doc.font('Helvetica').fontSize(9.5).fillColor(C.text).text(t, { lineGap: 2.2, ...o });
  doc.moveDown(0.12);
}

function bullet(t) {
  doc.font('Helvetica').fontSize(9.5).fillColor(C.text).text(`  •  ${t}`, { lineGap: 1.8 });
}

function mono(lines) {
  ensure(40 + lines.split('\n').length * 10);
  doc.font('Courier').fontSize(7.8).fillColor(C.mono).text(lines, { lineGap: 1.2 });
  doc.moveDown(0.3);
}

function tbl(headers, rows, colRatios) {
  const x0 = doc.page.margins.left;
  const w = doc.page.width - doc.page.margins.left - doc.page.margins.right;
  const cols = colRatios.map((r) => w * r);
  const pad = 4;
  const fsz = 8;

  function drawHeader() {
    const y = doc.y;
    doc.rect(x0, y, w, 18).fill('#edf2f7');
    let x = x0 + pad;
    doc.font('Helvetica-Bold').fontSize(fsz).fillColor(C.primary);
    headers.forEach((h, i) => {
      doc.text(h, x, y + 4, { width: cols[i] - pad * 2, lineBreak: false });
      x += cols[i];
    });
    doc.y = y + 20;
  }

  drawHeader();
  rows.forEach((row) => {
    if (doc.y > doc.page.height - 50) {
      doc.addPage();
      drawHeader();
    }
    let x = x0 + pad;
    const yStart = doc.y;
    doc.font('Helvetica').fontSize(fsz).fillColor(C.text);
    let maxH = 12;
    row.forEach((cell, i) => {
      const h = doc.heightOfString(String(cell), { width: cols[i] - pad * 2 });
      maxH = Math.max(maxH, h);
    });
    row.forEach((cell, i) => {
      doc.text(String(cell), x, yStart, { width: cols[i] - pad * 2, lineGap: 1 });
      x += cols[i];
    });
    doc.y = yStart + maxH + 4;
    doc.strokeColor(C.line).lineWidth(0.25).moveTo(x0, doc.y).lineTo(x0 + w, doc.y).stroke();
    doc.moveDown(0.15);
  });
  doc.moveDown(0.2);
}

function moduleBlock(name, controller, service, tables, desc, endpoints) {
  sub(name);
  p(desc);
  doc.font('Helvetica-Bold').fontSize(9).fillColor(C.accent).text('Camadas:', { continued: false });
  bullet(`Controller: ${controller}`);
  bullet(`Service: ${service || '(acesso direto via JPA Repository)'}`);
  bullet(`Tabelas: ${tables}`);
  if (endpoints && endpoints.length) {
    doc.moveDown(0.1);
    doc.font('Helvetica-Bold').fontSize(9).fillColor(C.accent).text('Endpoints:');
    endpoints.forEach(([m, r, d]) => {
      doc.font('Helvetica-Bold').fontSize(8.5).fillColor(C.accent).text(`  ${m} ${r}`, { continued: true });
      doc.font('Helvetica').fontSize(8.5).fillColor(C.text).text(` — ${d}`);
    });
  }
  doc.moveDown(0.25);
}

// ═══════════════════════════════════════════════════════════════
// CAPA
// ═══════════════════════════════════════════════════════════════
doc.font('Helvetica-Bold').fontSize(21).fillColor(C.primary)
  .text('QuickStock BackEnd', { align: 'center' });
doc.moveDown(0.2);
doc.font('Helvetica-Bold').fontSize(14).fillColor(C.secondary)
  .text('Documentação Técnica Completa', { align: 'center' });
doc.moveDown(0.15);
doc.font('Helvetica').fontSize(11).fillColor(C.muted)
  .text('Modelo Conceitual · Lógico · Físico', { align: 'center' });
doc.moveDown(0.8);
doc.font('Helvetica').fontSize(9.5).fillColor(C.muted)
  .text('Spring Boot 3.4 · Java 17 · PostgreSQL · JPA/Hibernate', { align: 'center' });
doc.text(`Versão do documento: ${new Date().toLocaleDateString('pt-BR')}`, { align: 'center' });
doc.moveDown(1);

p('Este documento descreve a arquitetura completa do backend QuickStock, correlacionando cada módulo da API com as tabelas do banco de dados PostgreSQL (quickstock). Inclui os três níveis de modelagem de dados — conceitual, lógico e físico — e o mapeamento entre entidades JPA, serviços REST e persistência.');

// ═══════════════════════════════════════════════════════════════
// 1. ARQUITETURA
// ═══════════════════════════════════════════════════════════════
section('1. Arquitetura do sistema');

sub('1.1 Visão geral');
p('O QuickStock BackEnd é uma API REST monolítica em Spring Boot que atende dois domínios de negócio integrados:');

bullet('Domínio PDV/Eventos — gestão de feiras, barracas, estoque local e vendas no ponto de venda (fluxo original).');
bullet('Domínio Marketplace B2B — catálogo de distribuidoras, sacola, checkout, endereços, pagamentos e acompanhamento de pedidos (fluxo mobile).');

sub('1.2 Camadas da aplicação');
mono(
`┌─────────────────────────────────────────────────────────┐
│  Cliente (App Mobile React Native / Swagger UI)         │
└──────────────────────────┬──────────────────────────────┘
                           │ HTTP/JSON  :8080/api/*
┌──────────────────────────▼──────────────────────────────┐
│  CONTROLLER  (@RestController)  — validação, HTTP       │
├─────────────────────────────────────────────────────────┤
│  SERVICE     (@Service)         — regras de negócio     │
├─────────────────────────────────────────────────────────┤
│  REPOSITORY  (JpaRepository)    — persistência JPA      │
├─────────────────────────────────────────────────────────┤
│  ENTITY      (@Entity)            — mapeamento ORM        │
└──────────────────────────┬──────────────────────────────┘
                           │ JDBC
┌──────────────────────────▼──────────────────────────────┐
│  PostgreSQL  — banco "quickstock"  (ddl-auto=update)    │
└─────────────────────────────────────────────────────────┘`,
);

sub('1.3 Stack tecnológica');
tbl(
  ['Componente', 'Tecnologia'],
  [
    ['Framework', 'Spring Boot 3.4.0'],
    ['Linguagem', 'Java 17'],
    ['ORM', 'Spring Data JPA / Hibernate 6'],
    ['Banco', 'PostgreSQL (quickstock)'],
    ['Autenticação', 'JWT (jjwt) + BCrypt'],
    ['Documentação API', 'SpringDoc OpenAPI (Swagger UI)'],
    ['Schema', 'Hibernate ddl-auto=update (sem Flyway)'],
    ['Seeds', 'data.sql + ApplicationRunners Java'],
  ],
  [0.3, 0.7],
);

// ═══════════════════════════════════════════════════════════════
// 2. MODELO CONCEITUAL
// ═══════════════════════════════════════════════════════════════
section('2. Modelo conceitual');

p('O modelo conceitual representa as entidades de negócio e seus relacionamentos, independente de implementação técnica. O QuickStock modela uma plataforma onde empresas compradoras (bares, quiosques) adquirem produtos de distribuidoras via marketplace, enquanto operam barracas em eventos com estoque e vendas próprias.');

sub('2.1 Entidades de negócio');
tbl(
  ['Entidade', 'Descrição conceitual'],
  [
    ['Empresa', 'Organização cadastrada. Pode ser COMPRADOR (cliente), DISTRIBUIDOR (fornecedor) ou PLATAFORMA.'],
    ['Perfil', 'Papel de acesso do usuário (Admin, Operador, etc.).'],
    ['Usuário', 'Pessoa que acessa o sistema, vinculada a uma empresa e um perfil.'],
    ['Produto', 'Item comercializado por uma empresa (geralmente o fornecedor).'],
    ['Evento', 'Feira ou evento temporal organizado por uma empresa.'],
    ['Barraca', 'Ponto de venda físico dentro de um evento.'],
    ['Estoque da Barraca', 'Quantidade de cada produto disponível na barraca.'],
    ['Pedido (PDV)', 'Venda realizada na barraca por um operador.'],
    ['Item do Pedido', 'Produto e quantidade vendidos em um pedido PDV.'],
    ['Pagamento', 'Forma e valor de pagamento de um pedido PDV.'],
    ['Endereço de Entrega', 'Local de entrega cadastrado pela empresa compradora.'],
    ['Forma de Pagamento Salva', 'Preferência de pagamento (tipo + apelido) da empresa.'],
    ['Solicitação de Compra', 'Pedido B2B do marketplace (comprador → fornecedor).'],
    ['Item da Solicitação', 'Produto encomendado em uma solicitação de compra.'],
  ],
  [0.28, 0.72],
);

sub('2.2 Diagrama entidade-relacionamento (conceitual)');
mono(
`                    ┌──────────┐
                    │  PERFIL  │
                    └────┬─────┘
                         │ 1
                         │ possui
                         │ N
┌────────────┐      ┌────▼─────┐      ┌──────────┐
│  EVENTO    │◄─N───│ EMPRESA  │──N──►│ PRODUTO  │
└─────┬──────┘ 1    └────┬─────┘ 1    └────┬─────┘
      │ 1                │ 1               │
      │ contém           │ possui          │ compõe
      │ N                │ N               │
┌─────▼──────┐    ┌──────▼───────┐   ┌─────▼──────────────┐
│  BARRACA   │    │   USUÁRIO    │   │ ITEM_SOLICITACAO   │
└─────┬──────┘    └──────┬───────┘   └─────────┬──────────┘
      │                  │                      │ N
      │                  │ solicita             │
      │                  │ N                    │ 1
      │            ┌─────▼──────────┐    ┌──────▼───────────┐
      │            │ SOLICITACAO    │◄───│ SOLICITACAO      │
      │            │ DE COMPRA      │ 1  │ DE COMPRA        │
      │            └────────────────┘    └──────────────────┘
      │
      ├──► ESTOQUE_BARRACA (N produtos)
      │
      └──► PEDIDO (PDV) ──► ITEM_PEDIDO / PAGAMENTO

EMPRESA ──► ENDERECO_ENTREGA
EMPRESA ──► FORMA_PAGAMENTO_SALVA`,
);

sub('2.3 Cardinalidades principais');
tbl(
  ['Relacionamento', 'Cardinalidade', 'Significado'],
  [
    ['Empresa → Usuário', '1:N', 'Uma empresa possui vários usuários.'],
    ['Empresa → Produto', '1:N', 'Fornecedor cataloga vários produtos.'],
    ['Empresa → Evento', '1:N', 'Empresa organiza vários eventos.'],
    ['Evento → Barraca', '1:N', 'Evento contém várias barracas.'],
    ['Barraca → Estoque', '1:N', 'Cada barraca controla estoque de N produtos.'],
    ['Barraca → Pedido', '1:N', 'Barraca registra várias vendas.'],
    ['Pedido → Itens/Pagamentos', '1:N', 'Pedido tem itens e pagamentos.'],
    ['Comprador → Solicitação', '1:N', 'Empresa compradora faz N pedidos B2B.'],
    ['Fornecedor → Solicitação', '1:N', 'Distribuidor recebe N pedidos.'],
    ['Solicitação → Itens', '1:N', 'Pedido B2B contém N produtos.'],
    ['Empresa → Endereços', '1:N', 'Comprador cadastra N endereços.'],
  ],
  [0.32, 0.18, 0.5],
);

// ═══════════════════════════════════════════════════════════════
// 3. MODELO LÓGICO
// ═══════════════════════════════════════════════════════════════
section('3. Modelo lógico');

p('O modelo lógico traduz as entidades conceituais em relações (tabelas), chaves primárias (PK), estrangeiras (FK) e restrições de integridade referencial.');

sub('3.1 Domínio PDV / Eventos');
tbl(
  ['Tabela lógica', 'PK', 'FKs', 'Atributos principais'],
  [
    ['perfis', 'id', '—', 'nome (UK), descricao'],
    ['empresas', 'id', '—', 'nome, cnpj (UK), telefone, tipo, descricao, logo_url, capa_url, criado_em'],
    ['usuarios', 'id', 'perfil_id, empresa_id', 'nome, email (UK), senha_hash, ativo, criado_em'],
    ['produtos', 'id', 'empresa_id', 'nome, preco_venda, unidade, descricao, imagem_url, ativo'],
    ['eventos', 'id', 'empresa_id', 'nome, data_inicio, data_fim, status'],
    ['barracas', 'id', 'evento_id, responsavel_id', 'nome, ativa'],
    ['estoque_barraca', 'id', 'barraca_id, produto_id', 'quantidade, atualizado_em | UK(barraca_id, produto_id)'],
    ['pedido', 'id', 'barraca_id, operador_id', 'valor_total, status, criado_em'],
    ['itens_pedido', 'id', 'pedido_id, produto_id', 'quantidade, preco_unitario, subtotal'],
    ['pagamentos', 'id', 'pedido_id', 'metodo, valor, status'],
  ],
  [0.22, 0.08, 0.28, 0.42],
);

sub('3.2 Domínio Marketplace B2B');
tbl(
  ['Tabela lógica', 'PK', 'FKs', 'Atributos principais'],
  [
    ['enderecos_entrega', 'id', 'empresa_id', 'apelido, logradouro, numero, complemento, bairro, cidade, uf, cep, principal'],
    ['formas_pagamento_salvas', 'id', 'empresa_id', 'tipo, apelido, principal'],
    ['solicitacoes_compra', 'id', 'empresa_compradora_id, empresa_fornecedora_id, usuario_solicitante_id', 'valor_total, status, observacao, metodo_pagamento, endereco (snapshot), taxa_entrega, criado_em'],
    ['itens_solicitacao_compra', 'id', 'solicitacao_id, produto_id', 'quantidade, preco_unitario, subtotal'],
  ],
  [0.24, 0.08, 0.34, 0.34],
);

sub('3.3 Diagrama lógico simplificado (FKs)');
mono(
`perfis ──< usuarios >── empresas ──< produtos
                │            │
                │            ├──< eventos ──< barracas ──< estoque_barraca >── produtos
                │            │                  │
                │            │                  └──< pedido ──< itens_pedido >── produtos
                │            │                           └──< pagamentos
                │            │
                │            ├──< enderecos_entrega
                │            ├──< formas_pagamento_salvas
                │            │
                └──< solicitacoes_compra (usuario_solicitante_id)
                     │  empresa_compradora_id ──> empresas
                     │  empresa_fornecedora_id ──> empresas
                     └──< itens_solicitacao_compra >── produtos`,
);

sub('3.4 Regras de integridade lógica');
bullet('Todo usuário pertence a exatamente uma empresa e um perfil.');
bullet('Produto pertence a uma empresa (tipicamente DISTRIBUIDOR ou PLATAFORMA).');
bullet('Estoque da barraca: par (barraca, produto) é único.');
bullet('Solicitação de compra referencia comprador, fornecedor e usuário solicitante distintos.');
bullet('Endereço no pedido B2B é copiado como snapshot (desnormalizado) além do FK opcional via enderecoEntregaId na criação.');
bullet('Empresa não pode solicitar compra de si mesma (regra de aplicação).');

// ═══════════════════════════════════════════════════════════════
// 4. MODELO FÍSICO
// ═══════════════════════════════════════════════════════════════
section('4. Modelo físico (PostgreSQL)');

p('Implementação concreta no SGBD PostgreSQL. O Hibernate gera/altera o schema via ddl-auto=update com base nas anotações @Entity.');

sub('4.1 Configuração física');
tbl(
  ['Parâmetro', 'Valor'],
  [
    ['SGBD', 'PostgreSQL'],
    ['Database', 'quickstock'],
    ['URL JDBC', 'jdbc:postgresql://localhost:5432/quickstock'],
    ['Dialect', 'org.hibernate.dialect.PostgreSQLDialect'],
    ['DDL', 'spring.jpa.hibernate.ddl-auto=update'],
    ['Encoding', 'UTF-8 (client_encoding, servlet, SQL init)'],
    ['Porta API', '8080'],
  ],
  [0.35, 0.65],
);

sub('4.2 Tipos de dados físicos por tabela');

function physicalTable(name, cols) {
  ensure(30 + cols.length * 11);
  doc.font('Helvetica-Bold').fontSize(9).fillColor(C.secondary).text(`Tabela: ${name}`);
  tbl(['Coluna', 'Tipo PostgreSQL', 'Restrições'], cols, [0.32, 0.28, 0.4]);
}

physicalTable('perfis', [
  ['id', 'BIGSERIAL', 'PRIMARY KEY'],
  ['nome', 'VARCHAR(50)', 'NOT NULL, UNIQUE'],
  ['descricao', 'VARCHAR(200)', 'NULL'],
]);

physicalTable('empresas', [
  ['id', 'BIGSERIAL', 'PRIMARY KEY'],
  ['nome', 'VARCHAR(150)', 'NOT NULL'],
  ['cnpj', 'VARCHAR(18)', 'NOT NULL, UNIQUE'],
  ['telefone', 'VARCHAR(20)', 'NULL'],
  ['tipo', 'VARCHAR(20)', 'DEFAULT COMPRADOR'],
  ['descricao', 'VARCHAR(300)', 'NULL'],
  ['logo_url', 'VARCHAR(500)', 'NULL'],
  ['capa_url', 'VARCHAR(500)', 'NULL'],
  ['criado_em', 'TIMESTAMP', 'NOT NULL, auto'],
]);

physicalTable('usuarios', [
  ['id', 'BIGSERIAL', 'PRIMARY KEY'],
  ['nome', 'VARCHAR(100)', 'NOT NULL'],
  ['email', 'VARCHAR(150)', 'NOT NULL, UNIQUE'],
  ['senha_hash', 'VARCHAR', 'NOT NULL'],
  ['perfil_id', 'BIGINT', 'FK → perfis(id)'],
  ['empresa_id', 'BIGINT', 'FK → empresas(id)'],
  ['ativo', 'INTEGER', 'DEFAULT 1'],
  ['criado_em', 'TIMESTAMP', 'NOT NULL, auto'],
]);

physicalTable('produtos', [
  ['id', 'BIGSERIAL', 'PRIMARY KEY'],
  ['empresa_id', 'BIGINT', 'FK → empresas(id)'],
  ['nome', 'VARCHAR(150)', 'NOT NULL'],
  ['preco_venda', 'NUMERIC(10,2)', 'NOT NULL'],
  ['unidade', 'VARCHAR(20)', 'NOT NULL'],
  ['descricao', 'VARCHAR(500)', 'NULL'],
  ['imagem_url', 'VARCHAR(500)', 'NULL'],
  ['ativo', 'INTEGER', 'DEFAULT 1'],
]);

physicalTable('eventos', [
  ['id', 'BIGSERIAL', 'PRIMARY KEY'],
  ['empresa_id', 'BIGINT', 'FK → empresas(id)'],
  ['nome', 'VARCHAR(150)', 'NOT NULL'],
  ['data_inicio', 'DATE', 'NOT NULL'],
  ['data_fim', 'DATE', 'NOT NULL'],
  ['status', 'VARCHAR(20)', 'DEFAULT planejado'],
]);

physicalTable('barracas', [
  ['id', 'BIGSERIAL', 'PRIMARY KEY'],
  ['evento_id', 'BIGINT', 'FK → eventos(id)'],
  ['responsavel_id', 'BIGINT', 'FK → usuarios(id)'],
  ['nome', 'VARCHAR(100)', 'NOT NULL'],
  ['ativa', 'INTEGER', 'DEFAULT 1'],
]);

physicalTable('estoque_barraca', [
  ['id', 'BIGSERIAL', 'PRIMARY KEY'],
  ['barraca_id', 'BIGINT', 'FK → barracas(id)'],
  ['produto_id', 'BIGINT', 'FK → produtos(id)'],
  ['quantidade', 'NUMERIC(10,3)', 'NOT NULL, DEFAULT 0'],
  ['atualizado_em', 'TIMESTAMP', 'auto @UpdateTimestamp'],
  ['(barraca_id, produto_id)', '—', 'UNIQUE CONSTRAINT'],
]);

physicalTable('pedido', [
  ['id', 'BIGSERIAL', 'PRIMARY KEY'],
  ['barraca_id', 'BIGINT', 'FK → barracas(id)'],
  ['operador_id', 'BIGINT', 'FK → usuarios(id)'],
  ['valor_total', 'NUMERIC(10,2)', 'NOT NULL'],
  ['status', 'VARCHAR(20)', 'DEFAULT aberto'],
  ['criado_em', 'TIMESTAMP', 'NOT NULL, auto'],
]);

physicalTable('itens_pedido', [
  ['id', 'BIGSERIAL', 'PRIMARY KEY'],
  ['pedido_id', 'BIGINT', 'FK → pedido(id)'],
  ['produto_id', 'BIGINT', 'FK → produtos(id)'],
  ['quantidade', 'NUMERIC(10,3)', 'NOT NULL'],
  ['preco_unitario', 'NUMERIC(10,2)', 'NOT NULL'],
  ['subtotal', 'NUMERIC(10,2)', 'NOT NULL'],
]);

physicalTable('pagamentos', [
  ['id', 'BIGSERIAL', 'PRIMARY KEY'],
  ['pedido_id', 'BIGINT', 'FK → pedido(id)'],
  ['metodo', 'VARCHAR(20)', 'NOT NULL'],
  ['valor', 'NUMERIC(10,2)', 'NOT NULL'],
  ['status', 'VARCHAR(20)', 'DEFAULT pendente'],
]);

physicalTable('enderecos_entrega', [
  ['id', 'BIGSERIAL', 'PRIMARY KEY'],
  ['empresa_id', 'BIGINT', 'FK → empresas(id)'],
  ['apelido', 'VARCHAR(80)', 'NOT NULL'],
  ['logradouro', 'VARCHAR(150)', 'NOT NULL'],
  ['numero', 'VARCHAR(20)', 'NOT NULL'],
  ['complemento', 'VARCHAR(80)', 'NULL'],
  ['bairro', 'VARCHAR(80)', 'NOT NULL'],
  ['cidade', 'VARCHAR(80)', 'NOT NULL'],
  ['uf', 'VARCHAR(2)', 'NOT NULL'],
  ['cep', 'VARCHAR(9)', 'NOT NULL'],
  ['principal', 'BOOLEAN', 'DEFAULT false'],
]);

physicalTable('formas_pagamento_salvas', [
  ['id', 'BIGSERIAL', 'PRIMARY KEY'],
  ['empresa_id', 'BIGINT', 'FK → empresas(id)'],
  ['tipo', 'VARCHAR(20)', 'NOT NULL — pix|credito|debito|dinheiro'],
  ['apelido', 'VARCHAR(80)', 'NOT NULL'],
  ['principal', 'BOOLEAN', 'DEFAULT false'],
]);

physicalTable('solicitacoes_compra', [
  ['id', 'BIGSERIAL', 'PRIMARY KEY'],
  ['empresa_compradora_id', 'BIGINT', 'FK → empresas(id)'],
  ['empresa_fornecedora_id', 'BIGINT', 'FK → empresas(id)'],
  ['usuario_solicitante_id', 'BIGINT', 'FK → usuarios(id)'],
  ['valor_total', 'NUMERIC(10,2)', 'NOT NULL'],
  ['status', 'VARCHAR(20)', 'DEFAULT enviada'],
  ['observacao', 'VARCHAR(500)', 'NULL'],
  ['metodo_pagamento', 'VARCHAR(20)', 'NULL'],
  ['endereco_resumo', 'VARCHAR(300)', 'NULL'],
  ['cep..uf', 'VARCHAR', 'Snapshot do endereço'],
  ['taxa_entrega', 'NUMERIC(10,2)', 'DEFAULT 0'],
  ['criado_em', 'TIMESTAMP', 'NOT NULL, auto'],
]);

physicalTable('itens_solicitacao_compra', [
  ['id', 'BIGSERIAL', 'PRIMARY KEY'],
  ['solicitacao_id', 'BIGINT', 'FK → solicitacoes_compra(id)'],
  ['produto_id', 'BIGINT', 'FK → produtos(id)'],
  ['quantidade', 'NUMERIC(10,3)', 'NOT NULL'],
  ['preco_unitario', 'NUMERIC(10,2)', 'NOT NULL'],
  ['subtotal', 'NUMERIC(10,2)', 'NOT NULL'],
]);

sub('4.3 Mapeamento JPA → Tabela física');
tbl(
  ['Classe @Entity', 'Tabela PostgreSQL', 'Arquivo'],
  [
    ['Perfil', 'perfis', 'entity/Perfil.java'],
    ['Empresa', 'empresas', 'entity/Empresa.java'],
    ['Usuario', 'usuarios', 'entity/Usuario.java'],
    ['Produto', 'produtos', 'entity/Produto.java'],
    ['Evento', 'eventos', 'entity/Evento.java'],
    ['Barraca', 'barracas', 'entity/Barraca.java'],
    ['EstoqueBarraca', 'estoque_barraca', 'entity/EstoqueBarraca.java'],
    ['Pedido', 'pedido', 'entity/Pedido.java'],
    ['ItemPedido', 'itens_pedido', 'entity/ItemPedido.java'],
    ['Pagamento', 'pagamentos', 'entity/Pagamento.java'],
    ['EnderecoEntrega', 'enderecos_entrega', 'entity/EnderecoEntrega.java'],
    ['FormaPagamentoSalva', 'formas_pagamento_salvas', 'entity/FormaPagamentoSalva.java'],
    ['SolicitacaoCompra', 'solicitacoes_compra', 'entity/SolicitacaoCompra.java'],
    ['ItemSolicitacaoCompra', 'itens_solicitacao_compra', 'entity/ItemSolicitacaoCompra.java'],
  ],
  [0.28, 0.32, 0.4],
);

// ═══════════════════════════════════════════════════════════════
// 5. MÓDULOS BACKEND ↔ BANCO
// ═══════════════════════════════════════════════════════════════
section('5. Módulos da API correlacionados ao banco');

p('Cada módulo abaixo indica quais tabelas lê ou grava, permitindo rastrear o fluxo da requisição HTTP até a persistência.');

moduleBlock(
  '5.1 Cadastro e autenticação',
  'CadastroController, UsuarioController',
  'CadastroService, JwtService',
  'empresas, usuarios, perfis',
  'Registra nova empresa compradora com usuário administrador. Login gera JWT. Senhas armazenadas com BCrypt em senha_hash.',
  [
    ['POST', '/api/cadastro', 'Cria empresa + usuário admin'],
    ['POST', '/api/usuarios/login', 'Autentica e retorna token'],
    ['POST', '/api/usuarios/register', 'Registro alternativo'],
    ['GET', '/api/usuarios/me', 'Perfil do token JWT'],
  ],
);

moduleBlock(
  '5.2 Empresas e perfis',
  'EmpresaController, PerfilController',
  '(JpaRepository direto)',
  'empresas, perfis',
  'CRUD administrativo de empresas e perfis de acesso.',
  [
    ['GET/POST/PUT/DELETE', '/api/empresas', 'CRUD empresas'],
    ['GET/POST/PUT/DELETE', '/api/perfis', 'CRUD perfis'],
  ],
);

moduleBlock(
  '5.3 Marketplace',
  'MarketplaceController',
  'MarketplaceService',
  'empresas (tipo DISTRIBUIDOR/PLATAFORMA), produtos',
  'Lista fornecedores ativos e catálogo de produtos por distribuidora. Filtra empresas por tipo e produtos ativos.',
  [
    ['GET', '/api/marketplace/fornecedores', 'Lista distribuidoras'],
    ['GET', '/api/marketplace/fornecedores/{id}/produtos', 'Catálogo'],
  ],
);

moduleBlock(
  '5.4 Produtos',
  'ProdutoController',
  'ProdutoUploadService',
  'produtos (+ arquivos em uploads/produtos/)',
  'CRUD de produtos por empresa. Upload de imagem salva arquivo local e grava imagem_url no banco.',
  [
    ['GET/POST/PUT/DELETE', '/api/produtos', 'CRUD produtos'],
    ['POST', '/api/produtos/upload', 'Upload imagem multipart'],
  ],
);

moduleBlock(
  '5.5 Solicitações de compra (Sacola / Checkout)',
  'SolicitacaoCompraController',
  'SolicitacaoCompraService',
  'solicitacoes_compra, itens_solicitacao_compra, enderecos_entrega (validação)',
  'Checkout B2B: valida fornecedor, pagamento, endereço; persiste pedido e itens; atualiza status demo (em_rota, entregue); retorna timeline e previsão.',
  [
    ['GET', '/api/solicitacoes-compra', 'Lista pedidos do comprador'],
    ['POST', '/api/solicitacoes-compra', 'Cria pedido (checkout)'],
    ['GET', '/api/solicitacoes-compra/{id}', 'Detalhe + acompanhamento'],
  ],
);

moduleBlock(
  '5.6 Endereços de entrega',
  'EnderecoEntregaController',
  'EnderecoEntregaService',
  'enderecos_entrega',
  'Cadastro e listagem de endereços por empresa. EnderecoSeedRunner popula dados demo.',
  [
    ['GET', '/api/enderecos?empresaId=', 'Lista endereços'],
    ['POST', '/api/enderecos', 'Cadastra endereço'],
  ],
);

moduleBlock(
  '5.7 Formas de pagamento',
  'FormaPagamentoSalvaController',
  'FormaPagamentoSalvaService',
  'formas_pagamento_salvas',
  'Preferências de pagamento por empresa. Não persiste dados de cartão — apenas tipo e apelido.',
  [
    ['GET', '/api/formas-pagamento', 'Lista formas'],
    ['POST', '/api/formas-pagamento', 'Salva forma'],
    ['DELETE', '/api/formas-pagamento/{id}', 'Remove'],
  ],
);

moduleBlock(
  '5.8 Financeiro',
  'FinanceiroController',
  'FinanceiroService',
  'solicitacoes_compra, itens_solicitacao_compra, pedido, itens_pedido, barracas',
  'Agrega dados reais de compras B2B e vendas PDV para gráficos. /resumo = 6 meses; /stock-dia = movimentos do dia.',
  [
    ['GET', '/api/financeiro/resumo', 'Lucros, compras, pedidos mensais'],
    ['GET', '/api/financeiro/stock-dia', 'Compras/vendas/lucro do dia'],
  ],
);

moduleBlock(
  '5.9 Notificações',
  'NotificacaoController',
  'NotificacaoService',
  'solicitacoes_compra, empresas (leitura)',
  'Gera notificações derivadas de status de pedidos B2B e mensagens promocionais demo. Não possui tabela própria — computado em runtime.',
  [['GET', '/api/notificacoes', 'Lista notificações da empresa']],
);

moduleBlock(
  '5.10 Eventos, barracas e estoque (PDV)',
  'EventoController, BarracaController, EstoqueBarracaController',
  'BarracaService',
  'eventos, barracas, estoque_barraca, produtos',
  'Gestão de feiras, pontos de venda e quantidades em estoque por barraca/produto.',
  [
    ['GET/POST/PUT/DELETE', '/api/eventos', 'CRUD eventos'],
    ['GET/POST/PUT/DELETE', '/api/barracas', 'CRUD barracas + estoque'],
    ['PUT', '/api/barracas/{id}/estoque', 'Atualiza estoque'],
    ['GET/POST/PATCH/DELETE', '/api/estoque-barraca', 'Estoque granular'],
  ],
);

moduleBlock(
  '5.11 Pedidos PDV e pagamentos',
  'PedidoController, ItemPedidoController, PagamentoController',
  '(JpaRepository + lógica no controller)',
  'pedido, itens_pedido, pagamentos, barracas, usuarios, produtos',
  'Fluxo de venda no ponto de venda da barraca: abrir pedido, adicionar itens, registrar pagamentos, alterar status.',
  [
    ['GET/POST/PUT/PATCH/DELETE', '/api/pedidos', 'Pedidos de venda'],
    ['GET/POST/DELETE', '/api/itens-pedido', 'Itens do pedido PDV'],
    ['GET/POST/PATCH/DELETE', '/api/pagamentos', 'Pagamentos PDV'],
  ],
);

// ═══════════════════════════════════════════════════════════════
// 6. FLUXOS DE DADOS
// ═══════════════════════════════════════════════════════════════
section('6. Fluxos de dados principais');

sub('6.1 Checkout marketplace (Sacola → Banco)');
mono(
`App Mobile                    Backend                         PostgreSQL
──────────                    ───────                         ──────────
SacolaScreen
  │ itens por fornecedor
  ▼
POST /solicitacoes-compra ──► SolicitacaoCompraService ──► INSERT solicitacoes_compra
  { comprador, fornecedor,         │ valida empresas           INSERT itens_solicitacao_compra
    itens[], pagamento,            │ valida endereco_entrega   (snapshot endereco no pedido)
    enderecoId, taxa }             │ calcula totais
                                   ▼
                              Response DTO ◄── SELECT + status demo
  ◄── pedidoId, etapas,
      previsao entrega`,);

sub('6.2 Stock do dia (Leitura agregada)');
mono(
`GET /financeiro/stock-dia ──► FinanceiroService.obterStockDia()
                                    │
                    ┌───────────────┴────────────────┐
                    ▼                                ▼
         SELECT solicitacoes_compra          SELECT pedido + itens
         WHERE criado_em = HOJE               (vendas barraca do dia)
         (compras B2B)                       (estimativa vendas)
                    │                                │
                    └───────────────┬────────────────┘
                                    ▼
                              StockDiaDTO (sem tabela própria)`,);

sub('6.3 Cadastro de conta');
mono(
`POST /api/cadastro ──► CadastroService
                           │ EXISTS email? → 409
                           │ EXISTS cnpj?  → 409
                           ├── INSERT empresas (tipo COMPRADOR default)
                           ├── BCrypt(senha) → senha_hash
                           └── INSERT usuarios (perfil Admin)`,);

// ═══════════════════════════════════════════════════════════════
// 7. SEEDS E DADOS INICIAIS
// ═══════════════════════════════════════════════════════════════
section('7. Seeds e população inicial');

tbl(
  ['Fonte', 'Tabelas afetadas', 'Descrição'],
  [
    ['data.sql', 'perfis, empresas, produtos', '10 distribuidoras + produtos; INSERT idempotente'],
    ['EnderecoSeedRunner', 'enderecos_entrega', 'Endereços demo por empresa compradora'],
    ['FormaPagamentoSeedRunner', 'formas_pagamento_salvas', 'PIX, crédito, débito demo'],
    ['FinanceiroSeedRunner', 'solicitacoes_compra, itens', 'Histórico de compras para gráficos'],
    ['MarketplaceDataFixRunner', 'empresas, produtos', 'Normalização de tipos e dados'],
  ],
  [0.22, 0.28, 0.5],
);

// ═══════════════════════════════════════════════════════════════
// 8. INFRA E EXCEÇÕES
// ═══════════════════════════════════════════════════════════════
section('8. Infraestrutura transversal');

sub('8.1 Componentes sem tabela própria');
tbl(
  ['Componente', 'Função', 'Relação com banco'],
  [
    ['JwtService', 'Token JWT', 'Lê usuarios via claim; não persiste sessão'],
    ['GlobalExceptionHandler', 'Erros JSON padronizados', 'Não persiste'],
    ['NotificacaoService', 'Notificações computadas', 'Lê solicitacoes_compra, empresas'],
    ['ProdutoUploadService', 'Arquivos de imagem', 'Grava imagem_url em produtos'],
    ['WebConfig', 'CORS /api/**', 'Sem banco'],
    ['StockDiaDTO / FinanceiroResumoDTO', 'Agregações', 'Somente leitura de tabelas existentes'],
  ],
  [0.26, 0.34, 0.4],
);

sub('8.2 Valores de domínio relevantes');
tbl(
  ['Campo', 'Valores'],
  [
    ['empresas.tipo', 'COMPRADOR | DISTRIBUIDOR | PLATAFORMA'],
    ['solicitacoes_compra.status', 'enviada | confirmada | em_rota | entregue | cancelada (+ demo)'],
    ['formas_pagamento_salvas.tipo', 'pix | credito | debito | dinheiro'],
    ['pagamentos.metodo', 'dinheiro | credito | debito | pix'],
    ['pedido.status', 'aberto | fechado | cancelado (PDV)'],
    ['eventos.status', 'planejado | em_andamento | encerrado'],
  ],
  [0.35, 0.65],
);

// ═══════════════════════════════════════════════════════════════
// 9. RESUMO
// ═══════════════════════════════════════════════════════════════
section('9. Resumo dos três modelos');

tbl(
  ['Nível', 'O que representa', 'Artefatos no projeto'],
  [
    ['Conceitual', 'Entidades de negócio e relações', 'Seção 2 — EMPRESA, PEDIDO B2B, BARRACA...'],
    ['Lógico', 'Tabelas, PKs, FKs, cardinalidades', 'Seção 3 — 15 tabelas, diagrama FK'],
    ['Físico', 'PostgreSQL, tipos, constraints JPA', 'Seção 4 — DDL gerado pelo Hibernate'],
  ],
  [0.18, 0.42, 0.4],
);

p('Total: 15 tabelas físicas · 14 entidades JPA · 16 controllers REST · 2 domínios de negócio (PDV + Marketplace) integrados pelo cadastro de Empresa e catálogo de Produto.');

// ═══════════════════════════════════════════════════════════════
// RODAPÉ
// ═══════════════════════════════════════════════════════════════
const range = doc.bufferedPageRange();
for (let i = range.start; i < range.start + range.count; i++) {
  doc.switchToPage(i);
  doc.font('Helvetica').fontSize(7.5).fillColor(C.muted)
    .text(
      `QuickStock BackEnd · Documentação Completa · Página ${i + 1}/${range.count}`,
      doc.page.margins.left,
      doc.page.height - 34,
      { align: 'center', width: doc.page.width - doc.page.margins.left - doc.page.margins.right },
    );
}

doc.end();
process.stdout.write(`PDF gerado: ${outputPath}\n`);
