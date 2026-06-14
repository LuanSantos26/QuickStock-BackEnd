const fs = require('fs');
const path = require('path');
const PDFDocument = require('pdfkit');

const outputPath = path.join(__dirname, 'resumo-alteracoes-banco-quickstock.pdf');

const doc = new PDFDocument({
  size: 'A4',
  margins: { top: 56, bottom: 56, left: 56, right: 56 },
  info: {
    Title: 'QuickStock - Resumo de Alterações no Banco de Dados',
    Author: 'QuickStock Team',
    Subject: 'Histórico de schema PostgreSQL',
    CreationDate: new Date(),
  },
});

doc.pipe(fs.createWriteStream(outputPath));

const colors = {
  primary: '#1a365d',
  secondary: '#2c5282',
  accent: '#3182ce',
  text: '#1a202c',
  muted: '#4a5568',
  line: '#cbd5e0',
  tableHeader: '#edf2f7',
};

function hr() {
  doc.moveDown(0.4);
  doc.strokeColor(colors.line).lineWidth(0.5)
    .moveTo(doc.page.margins.left, doc.y)
    .lineTo(doc.page.width - doc.page.margins.right, doc.y)
    .stroke();
  doc.moveDown(0.6);
}

function sectionTitle(text) {
  if (doc.y > doc.page.height - 120) doc.addPage();
  doc.moveDown(0.8);
  doc.font('Helvetica-Bold').fontSize(14).fillColor(colors.primary).text(text);
  doc.moveDown(0.3);
  hr();
}

function subTitle(text) {
  if (doc.y > doc.page.height - 80) doc.addPage();
  doc.font('Helvetica-Bold').fontSize(11).fillColor(colors.secondary).text(text);
  doc.moveDown(0.25);
}

function body(text, opts = {}) {
  doc.font('Helvetica').fontSize(10).fillColor(colors.text)
    .text(text, { lineGap: 3, ...opts });
  doc.moveDown(0.2);
}

function bullet(text) {
  doc.font('Helvetica').fontSize(10).fillColor(colors.text)
    .text(`• ${text}`, { indent: 12, lineGap: 2 });
}

function tableRow(cols, isHeader = false) {
  const startX = doc.page.margins.left;
  const tableWidth = doc.page.width - doc.page.margins.left - doc.page.margins.right;
  const colWidths = [tableWidth * 0.28, tableWidth * 0.72];
  const rowHeight = isHeader ? 22 : 0;
  const y = doc.y;

  if (y > doc.page.height - 60) doc.addPage();

  if (isHeader) {
    doc.rect(startX, y, tableWidth, rowHeight).fill(colors.tableHeader);
    doc.fillColor(colors.primary);
    doc.font('Helvetica-Bold').fontSize(9);
  } else {
    doc.font('Helvetica').fontSize(9).fillColor(colors.text);
  }

  let x = startX + 6;
  cols.forEach((col, i) => {
    const textY = isHeader ? y + 6 : doc.y;
    if (isHeader) {
      doc.text(col, x, textY, { width: colWidths[i] - 8, lineBreak: false });
    } else {
      doc.text(col, x, doc.y, { width: colWidths[i] - 8, lineGap: 2 });
    }
    x += colWidths[i];
  });

  if (isHeader) {
    doc.y = y + rowHeight + 4;
  } else {
    doc.moveDown(0.35);
    doc.strokeColor(colors.line).lineWidth(0.3)
      .moveTo(startX, doc.y)
      .lineTo(startX + tableWidth, doc.y)
      .stroke();
    doc.moveDown(0.25);
  }
}

// ── Capa ───────────────────────────────────────────────────────
doc.font('Helvetica-Bold').fontSize(24).fillColor(colors.primary)
  .text('QuickStock', { align: 'center' });
doc.moveDown(0.3);
doc.font('Helvetica-Bold').fontSize(16).fillColor(colors.secondary)
  .text('Resumo de Alterações no Banco de Dados', { align: 'center' });
doc.moveDown(1.2);
doc.font('Helvetica').fontSize(11).fillColor(colors.muted)
  .text('PostgreSQL · banco quickstock · projeto QuickStock-BackEnd', { align: 'center' });
doc.moveDown(0.4);
doc.text(`Gerado em: ${new Date().toLocaleDateString('pt-BR', {
  day: '2-digit', month: 'long', year: 'numeric',
})}`, { align: 'center' });

doc.moveDown(2);
body('Este documento descreve todas as tabelas, colunas e dados iniciais (seeds) do banco de dados QuickStock, desde o schema funcional original até as extensões do marketplace B2B, endereços, formas de pagamento e pedidos de compra implementados durante o desenvolvimento do projeto mobile.');

// ── 1. Configuração ────────────────────────────────────────────
sectionTitle('1. Configuração do banco');

body('SGBD: PostgreSQL');
body('Nome do banco: quickstock');
body('Conexão padrão: jdbc:postgresql://localhost:5432/quickstock');
body('Estratégia de schema: spring.jpa.hibernate.ddl-auto=update');
body('Migrations versionadas: não há Flyway nem Liquibase. O Hibernate cria e altera tabelas automaticamente com base nas entidades JPA (@Entity).');
body('Dados iniciais: spring.sql.init.mode=always executa data.sql a cada startup, com INSERTs idempotentes (WHERE NOT EXISTS).');
body('Seeds adicionais: ApplicationRunners Java (EnderecoSeedRunner, FormaPagamentoSeedRunner, FinanceiroSeedRunner, MarketplaceDataFixRunner).');

// ── 2. Schema original ─────────────────────────────────────────
sectionTitle('2. Schema original (commit 8654053 — funcional database)');

body('Dez tabelas base para gestão de eventos, barracas, estoque e vendas no ponto de venda:');

const baseTables = [
  ['perfis', 'Papéis de usuário (Admin, Operador, etc.). Campos: id, nome, descricao.'],
  ['empresas', 'Cadastro de empresas. Campos: id, nome, cnpj (único), telefone, criado_em.'],
  ['usuarios', 'Usuários do sistema. FK perfil_id → perfis, empresa_id → empresas. Campos: nome, email (único), senha_hash, ativo, criado_em.'],
  ['produtos', 'Catálogo por empresa. FK empresa_id → empresas. Campos: nome, preco_venda, unidade, descricao, imagem_url, ativo.'],
  ['eventos', 'Eventos/feiras da empresa. FK empresa_id. Campos: nome, data_inicio, data_fim, status.'],
  ['barracas', 'Pontos de venda no evento. FK evento_id, responsavel_id → usuarios. Campos: nome, ativa.'],
  ['estoque_barraca', 'Estoque por barraca/produto (único por par). Campos: quantidade, atualizado_em.'],
  ['pedido', 'Pedidos de venda na barraca. FK barraca_id, operador_id. Campos: valor_total, status, criado_em.'],
  ['itens_pedido', 'Itens do pedido de venda. FK pedido_id, produto_id. Campos: quantidade, preco_unitario, subtotal.'],
  ['pagamentos', 'Pagamentos do pedido de venda. FK pedido_id. Campos: metodo (dinheiro/credito/debito/pix), valor, status.'],
];

tableRow(['Tabela', 'Descrição'], true);
baseTables.forEach((row) => tableRow(row));

doc.moveDown(0.5);
subTitle('Relacionamentos principais (schema original)');
bullet('Empresa → Usuários, Produtos, Eventos');
bullet('Evento → Barracas → Estoque e Pedidos');
bullet('Pedido → Itens e Pagamentos');

// ── 3. Alterações marketplace ──────────────────────────────────
sectionTitle('3. Alterações do marketplace (commit 4524146)');

body('Commit: "marketplace, pedidos, endereco e formas de pagamento". Foram adicionadas 4 novas tabelas e a tabela empresas foi estendida para suportar múltiplos fornecedores no app mobile.');

subTitle('3.1 Colunas novas em empresas');
const empCols = [
  ['tipo', 'VARCHAR(20), default COMPRADOR. Valores: COMPRADOR, DISTRIBUIDOR, PLATAFORMA.'],
  ['descricao', 'VARCHAR(300). Texto exibido no marketplace.'],
  ['logo_url', 'VARCHAR(500). URL da logo do fornecedor.'],
  ['capa_url', 'VARCHAR(500). URL da imagem de capa.'],
];
tableRow(['Coluna', 'Detalhes'], true);
empCols.forEach((row) => tableRow(row));

doc.moveDown(0.4);
subTitle('3.2 Tabela enderecos_entrega (nova)');
body('Endereços de entrega salvos por empresa compradora.');
const endCols = [
  ['id', 'BIGSERIAL PK'],
  ['empresa_id', 'FK → empresas (NOT NULL)'],
  ['apelido', 'VARCHAR(80) — ex.: "Depósito", "Casa"'],
  ['logradouro, numero, complemento', 'Endereço completo'],
  ['bairro, cidade, uf, cep', 'Localização'],
  ['principal', 'BOOLEAN — endereço padrão'],
];
endCols.forEach((row) => bullet(`${row[0]}: ${row[1]}`));

doc.moveDown(0.3);
subTitle('3.3 Tabela formas_pagamento_salvas (nova)');
body('Formas de pagamento favoritas por empresa (não armazena dados sensíveis de cartão).');
[
  'empresa_id → FK empresas',
  'tipo → pix | credito | debito | dinheiro',
  'apelido → nome exibido no app',
  'principal → boolean',
].forEach(bullet);

doc.moveDown(0.3);
subTitle('3.4 Tabela solicitacoes_compra (nova)');
body('Pedidos B2B do marketplace (compra de distribuidor para empresa compradora). Substitui o fluxo de "sacola" no app mobile.');
const solCols = [
  ['empresa_compradora_id', 'FK → empresas'],
  ['empresa_fornecedora_id', 'FK → empresas (distribuidor)'],
  ['usuario_solicitante_id', 'FK → usuarios'],
  ['valor_total', 'DECIMAL(10,2)'],
  ['status', 'VARCHAR(20) — enviada, confirmada, em_rota, entregue, cancelada'],
  ['observacao', 'VARCHAR(500)'],
  ['metodo_pagamento', 'VARCHAR(20)'],
  ['endereco_resumo', 'VARCHAR(300) — texto resumido'],
  ['cep, logradouro, numero, complemento, bairro, cidade, uf', 'Snapshot do endereço no pedido'],
  ['taxa_entrega', 'DECIMAL(10,2)'],
  ['criado_em', 'TIMESTAMP'],
];
tableRow(['Campo / grupo', 'Descrição'], true);
solCols.forEach((row) => tableRow(row));

doc.moveDown(0.3);
subTitle('3.5 Tabela itens_solicitacao_compra (nova)');
[
  'solicitacao_id → FK solicitacoes_compra',
  'produto_id → FK produtos',
  'quantidade DECIMAL(10,3)',
  'preco_unitario, subtotal DECIMAL(10,2)',
].forEach(bullet);

// ── 4. Seeds ───────────────────────────────────────────────────
sectionTitle('4. Dados iniciais (seeds)');

subTitle('4.1 data.sql');
body('Script SQL executado a cada inicialização da aplicação:');
bullet('Perfil Admin');
bullet('Empresa plataforma QuickStock Distribuidora (tipo PLATAFORMA)');
bullet('10 distribuidoras fictícias (tipo DISTRIBUIDOR) com nome, CNPJ, descrição, logo e capa');
bullet('Produtos de bebidas vinculados a cada distribuidora (cervejas, refrigerantes, etc.)');

subTitle('4.2 Runners Java');
bullet('EnderecoSeedRunner — cria endereços demo para empresas compradoras');
bullet('FormaPagamentoSeedRunner — cria PIX/cartão/dinheiro demo por empresa');
bullet('FinanceiroSeedRunner — gera solicitacoes_compra históricas (meses anteriores) para alimentar gráficos financeiros');
bullet('MarketplaceDataFixRunner — corrige/normaliza dados de marketplace existentes');

// ── 5. O que NÃO mudou no banco ────────────────────────────────
sectionTitle('5. Funcionalidades sem alteração de schema');

body('Durante o desenvolvimento do app mobile, várias features foram implementadas sem criar novas tabelas:');

bullet('Stock do dia / Carteira — endpoint GET /api/financeiro/stock-dia consulta solicitacoes_compra existentes (compras) e estima vendas; não há tabela financeira dedicada.');
bullet('Acompanhamento de pedido (estilo iFood) — status e previsão de entrega são calculados em SolicitacaoCompraService; campos previsaoEntregaMinutos/Label existem apenas no DTO de resposta, não no banco.');
bullet('Cartões e chaves PIX detalhados no mobile — persistência local/mock no app; o backend só guarda tipo + apelido em formas_pagamento_salvas.');
bullet('Sacola multi-fornecedor — lógica no app (PurchaseCartContext); checkout grava N registros em solicitacoes_compra (um por fornecedor).');

// ── 6. Resumo quantitativo ─────────────────────────────────────
sectionTitle('6. Resumo quantitativo');

tableRow(['Métrica', 'Valor'], true);
[
  ['Tabelas no schema original', '10'],
  ['Tabelas adicionadas', '4 (enderecos_entrega, formas_pagamento_salvas, solicitacoes_compra, itens_solicitacao_compra)'],
  ['Tabelas alteradas', '1 (empresas — +5 colunas)'],
  ['Total de entidades JPA atuais', '15'],
  ['Commits principais de banco', '8654053 (base) → 4524146 (marketplace)'],
].forEach((row) => tableRow(row));

// ── 7. Diagrama textual ────────────────────────────────────────
sectionTitle('7. Visão geral das relações (pós-marketplace)');

body('Marketplace B2B (novo fluxo):');
body('Empresa (COMPRADOR) ──► SolicitacaoCompra ◄── Empresa (DISTRIBUIDOR/PLATAFORMA)');
body('                              │');
body('                              ├── ItensSolicitacaoCompra ──► Produto');
body('                              └── Usuario (solicitante)');
body('');
body('Cadastros auxiliares:');
body('Empresa ──► EnderecoEntrega');
body('Empresa ──► FormaPagamentoSalva');
body('');
body('Ponto de venda (fluxo original, inalterado):');
body('Empresa ──► Evento ──► Barraca ──► Pedido ──► ItemPedido / Pagamento');
body('Barraca ──► EstoqueBarraca ──► Produto');

// ── Rodapé em cada página ──────────────────────────────────────
const range = doc.bufferedPageRange();
for (let i = range.start; i < range.start + range.count; i++) {
  doc.switchToPage(i);
  doc.font('Helvetica').fontSize(8).fillColor(colors.muted)
    .text(
      `QuickStock — Resumo Banco de Dados · Página ${i + 1} de ${range.count}`,
      doc.page.margins.left,
      doc.page.height - 40,
      { align: 'center', width: doc.page.width - doc.page.margins.left - doc.page.margins.right },
    );
}

doc.end();

doc.on('finish', () => {
  console.log(`PDF gerado: ${outputPath}`);
});
