const fs = require('fs');
const path = require('path');
const PDFDocument = require('pdfkit');

const outputPath = path.join(__dirname, 'documentacao-banco-quickstock.pdf');

const doc = new PDFDocument({
  size: 'A4',
  margins: { top: 56, bottom: 56, left: 56, right: 56 },
  info: {
    Title: 'QuickStock - Documentação Completa do Banco de Dados',
    Author: 'QuickStock Team',
    Subject: 'Schema PostgreSQL e histórico de alterações',
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

function ensureSpace(min = 80) {
  if (doc.y > doc.page.height - min) doc.addPage();
}

function sectionTitle(text) {
  ensureSpace(120);
  doc.moveDown(0.8);
  doc.font('Helvetica-Bold').fontSize(14).fillColor(colors.primary).text(text);
  doc.moveDown(0.3);
  hr();
}

function subTitle(text) {
  ensureSpace(80);
  doc.font('Helvetica-Bold').fontSize(11).fillColor(colors.secondary).text(text);
  doc.moveDown(0.25);
}

function body(text, opts = {}) {
  ensureSpace(40);
  doc.font('Helvetica').fontSize(10).fillColor(colors.text)
    .text(text, { lineGap: 3, ...opts });
  doc.moveDown(0.2);
}

function bullet(text) {
  ensureSpace(30);
  doc.font('Helvetica').fontSize(10).fillColor(colors.text)
    .text(`• ${text}`, { indent: 12, lineGap: 2 });
}

function mono(text) {
  ensureSpace(30);
  doc.font('Courier').fontSize(9).fillColor(colors.text)
    .text(text, { indent: 8, lineGap: 2 });
}

function tableRow(cols, isHeader = false, widths = [0.28, 0.72]) {
  const startX = doc.page.margins.left;
  const tableWidth = doc.page.width - doc.page.margins.left - doc.page.margins.right;
  const colWidths = widths.map((w) => tableWidth * w);
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

function table3(cols, isHeader = false) {
  tableRow(cols, isHeader, [0.22, 0.28, 0.50]);
}

// ── Capa ───────────────────────────────────────────────────────
doc.font('Helvetica-Bold').fontSize(26).fillColor(colors.primary)
  .text('QuickStock', { align: 'center' });
doc.moveDown(0.3);
doc.font('Helvetica-Bold').fontSize(16).fillColor(colors.secondary)
  .text('Documentação Completa do Banco de Dados', { align: 'center' });
doc.moveDown(0.5);
doc.font('Helvetica').fontSize(12).fillColor(colors.muted)
  .text('Schema, alterações e dados iniciais', { align: 'center' });
doc.moveDown(1.2);
doc.font('Helvetica').fontSize(11).fillColor(colors.muted)
  .text('PostgreSQL · banco quickstock · QuickStock-BackEnd', { align: 'center' });
doc.moveDown(0.4);
doc.text(`Gerado em: ${new Date().toLocaleDateString('pt-BR', {
  day: '2-digit', month: 'long', year: 'numeric',
})}`, { align: 'center' });

doc.moveDown(2);
body('Este documento descreve o banco de dados do QuickStock de ponta a ponta: configuração, schema original, todas as alterações feitas durante o desenvolvimento do projeto (marketplace B2B, checkout, estoque, códigos de produto, cartões salvos), explicação detalhada de cada tabela e coluna, regras de negócio de estoque, seeds e runners de inicialização.');

// ── 1. Configuração ────────────────────────────────────────────
sectionTitle('1. Configuração e estratégia de schema');

body('SGBD: PostgreSQL');
body('Nome do banco: quickstock');
body('Conexão padrão: jdbc:postgresql://localhost:5432/quickstock');
body('Porta da API: 8080 (Spring Boot)');
body('Estratégia de schema: spring.jpa.hibernate.ddl-auto=update — o Hibernate cria e altera tabelas automaticamente com base nas entidades JPA (@Entity).');
body('Migrations versionadas: não há Flyway nem Liquibase. Alterações incrementais são aplicadas via data.sql (ALTER TABLE idempotentes) e entidades Java.');
body('Dados iniciais: spring.sql.init.mode=always executa src/main/resources/data.sql a cada startup.');
body('Seeds adicionais: ApplicationRunners Java executados na inicialização (EnderecoSeedRunner, FormaPagamentoSeedRunner, FinanceiroSeedRunner, MarketplaceDataFixRunner, ProdutoCatalogoFixRunner).');
body('Uploads de imagens: arquivos em QuickStock-BackEnd/uploads/produtos/ e uploads/empresas/, servidos como URLs relativas (/uploads/...).');

// ── 2. Cronologia ──────────────────────────────────────────────
sectionTitle('2. Cronologia das alterações');

subTitle('Fase 1 — Schema original (PDV / eventos)');
body('Commit base "funcional database" (8654053). Dez tabelas para gestão de empresas, usuários, produtos, eventos, barracas, estoque por barraca e vendas no ponto de venda (PDV).');

subTitle('Fase 2 — Marketplace B2B');
body('Commit "marketplace, pedidos, endereco e formas de pagamento" (4524146). Quatro tabelas novas e extensão da tabela empresas para suportar compradores e distribuidores no app mobile.');

subTitle('Fase 3 — Checkout e pedido marketplace');
body('Extensão da tabela pedido para registrar compras B2B além do PDV. Colunas adicionadas via data.sql: tipo (pdv | marketplace), empresas compradora/fornecedora, endereço (FK + snapshot), método de pagamento, taxa de entrega, observação. barraca_id tornou-se opcional (nullable).');
body('Nova coluna pedido_id em solicitacoes_compra liga a solicitação ao registro financeiro em pedido.');
body('Nova coluna referencia_pagamento em pagamentos (ex.: ID da transação PIX ou últimos dígitos do cartão).');

subTitle('Fase 4 — Estoque e códigos de produto');
body('Coluna estoque (NUMERIC 10,3) em produtos — quantidade disponível no catálogo do fornecedor ou do comprador.');
body('Colunas codigo (único, VARCHAR 40) e codigo_origem (VARCHAR 40) — identificador fixo por produto e rastreio de produto recebido de fornecedor.');
body('Coluna estoque_comprador_creditado (BOOLEAN) em solicitacoes_compra — evita creditar estoque duas vezes na entrega.');
body('Serviço EstoqueProdutoService: debita estoque do fornecedor na compra; credita estoque do comprador na entrega (status entregue).');
body('ProdutoCatalogoFixRunner: garante IDs fixos 1001–1025 e códigos MKT-* para os 15 produtos oficiais do marketplace.');

subTitle('Fase 5 — Cartões salvos e catálogo parceiro');
body('Nova tabela cartoes_pagamento_salvos — armazena cartões mascarados por empresa (sem CVV). Separada de formas_pagamento_salvas (tipos genéricos: PIX, crédito, débito).');
body('Tipo INATIVO em empresas — distribuidoras legadas desativadas, mantendo histórico.');
body('Três distribuidoras parceiras oficiais: Casa dos Vinhos, Cervejaria Caruaru, Whisky Labs — 5 produtos cada, com estoque inicial e imagens reais.');

// ── 3. Schema original detalhado ───────────────────────────────
sectionTitle('3. Schema original — dez tabelas base');

body('O núcleo do sistema cobre autenticação, catálogo, eventos/feiras e vendas na barraca.');

subTitle('3.1 perfis');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL — chave primária'],
  ['nome', 'VARCHAR(50) UNIQUE — ex.: Admin, Operador'],
  ['descricao', 'VARCHAR(200) — texto explicativo do papel'],
].forEach((r) => tableRow(r));

subTitle('3.2 empresas');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL PK'],
  ['nome', 'VARCHAR(150) — razão social ou nome fantasia'],
  ['cnpj', 'VARCHAR(18) UNIQUE NOT NULL'],
  ['telefone', 'VARCHAR(20)'],
  ['criado_em', 'TIMESTAMP — preenchido automaticamente'],
].forEach((r) => tableRow(r));
body('Nota: colunas de marketplace (tipo, descricao, logo_url, capa_url) foram adicionadas na Fase 2 — ver seção 4.');

subTitle('3.3 usuarios');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL PK'],
  ['nome', 'VARCHAR(100)'],
  ['email', 'VARCHAR(150) UNIQUE NOT NULL'],
  ['senha_hash', 'VARCHAR — hash BCrypt da senha'],
  ['perfil_id', 'FK → perfis NOT NULL'],
  ['empresa_id', 'FK → empresas NOT NULL'],
  ['ativo', 'INTEGER default 1 (1=ativo, 0=inativo)'],
  ['criado_em', 'TIMESTAMP'],
].forEach((r) => tableRow(r));

subTitle('3.4 produtos');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL PK'],
  ['empresa_id', 'FK → empresas NOT NULL — dono do catálogo'],
  ['nome', 'VARCHAR(300) — ampliado de 150 para nomes longos de bebidas'],
  ['preco_venda', 'NUMERIC(10,2) NOT NULL'],
  ['unidade', 'VARCHAR(20) — ex.: UN, CX, LT'],
  ['descricao', 'VARCHAR(500)'],
  ['imagem_url', 'VARCHAR(500) — caminho relativo /uploads/produtos/...'],
  ['ativo', 'INTEGER default 1'],
  ['estoque', 'NUMERIC(10,3) — adicionado na Fase 4'],
  ['codigo', 'VARCHAR(40) UNIQUE NOT NULL — ex.: MKT-CDV-001, EMP-3-0001'],
  ['codigo_origem', 'VARCHAR(40) — código do fornecedor quando produto foi recebido'],
].forEach((r) => tableRow(r));

subTitle('3.5 eventos');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL PK'],
  ['empresa_id', 'FK → empresas NOT NULL'],
  ['nome', 'VARCHAR(150)'],
  ['data_inicio', 'DATE NOT NULL'],
  ['data_fim', 'DATE NOT NULL'],
  ['status', 'VARCHAR(20) default planejado — planejado | ativo | encerrado'],
].forEach((r) => tableRow(r));

subTitle('3.6 barracas');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL PK'],
  ['evento_id', 'FK → eventos NOT NULL'],
  ['nome', 'VARCHAR(100)'],
  ['responsavel_id', 'FK → usuarios NOT NULL'],
  ['ativa', 'INTEGER default 1'],
].forEach((r) => tableRow(r));

subTitle('3.7 estoque_barraca');
body('Estoque físico por barraca e produto. Constraint UNIQUE (barraca_id, produto_id).');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL PK'],
  ['barraca_id', 'FK → barracas NOT NULL'],
  ['produto_id', 'FK → produtos NOT NULL'],
  ['quantidade', 'NUMERIC(10,3) default 0'],
  ['atualizado_em', 'TIMESTAMP — atualizado automaticamente'],
].forEach((r) => tableRow(r));

subTitle('3.8 pedido');
body('Registra vendas PDV na barraca e, desde a Fase 3, pedidos marketplace B2B.');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL PK'],
  ['tipo', 'VARCHAR(20) default pdv — pdv | marketplace'],
  ['barraca_id', 'FK → barracas — NULL para pedidos marketplace'],
  ['operador_id', 'FK → usuarios NOT NULL — quem registrou'],
  ['empresa_compradora_id', 'FK → empresas — marketplace'],
  ['empresa_fornecedora_id', 'FK → empresas — distribuidor'],
  ['endereco_entrega_id', 'FK → enderecos_entrega — referência ao endereço salvo'],
  ['endereco_resumo', 'VARCHAR(300) — texto legível'],
  ['cep, logradouro, numero, complemento', 'Snapshot do endereço no momento da compra'],
  ['bairro, cidade, uf', 'Localização snapshot'],
  ['metodo_pagamento', 'VARCHAR(20) — pix | credito | debito | dinheiro'],
  ['taxa_entrega', 'NUMERIC(10,2) default 0'],
  ['observacao', 'VARCHAR(500)'],
  ['valor_total', 'NUMERIC(10,2) NOT NULL'],
  ['status', 'VARCHAR(20) default aberto'],
  ['criado_em', 'TIMESTAMP'],
].forEach((r) => tableRow(r));

subTitle('3.9 itens_pedido');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL PK'],
  ['pedido_id', 'FK → pedido NOT NULL'],
  ['produto_id', 'FK → produtos NOT NULL'],
  ['quantidade', 'NUMERIC(10,3) NOT NULL'],
  ['preco_unitario', 'NUMERIC(10,2) NOT NULL'],
  ['subtotal', 'NUMERIC(10,2) NOT NULL'],
].forEach((r) => tableRow(r));

subTitle('3.10 pagamentos');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL PK'],
  ['pedido_id', 'FK → pedido NOT NULL'],
  ['metodo', 'VARCHAR(20) — dinheiro | credito | debito | pix'],
  ['valor', 'NUMERIC(10,2) NOT NULL'],
  ['status', 'VARCHAR(20) default pendente — pendente | confirmado | cancelado'],
  ['referencia_pagamento', 'VARCHAR(120) — ID PIX, NSU ou referência externa'],
].forEach((r) => tableRow(r));

// ── 4. Marketplace ─────────────────────────────────────────────
sectionTitle('4. Extensões do marketplace B2B');

subTitle('4.1 Colunas adicionadas em empresas');
tableRow(['Coluna', 'Detalhes'], true);
[
  ['tipo', 'VARCHAR(20) default COMPRADOR. Valores: COMPRADOR, DISTRIBUIDOR, PLATAFORMA, INATIVO'],
  ['descricao', 'VARCHAR(300) — texto exibido na vitrine do fornecedor'],
  ['logo_url', 'VARCHAR(500) — logo do distribuidor'],
  ['capa_url', 'VARCHAR(500) — banner/capa da loja'],
].forEach((r) => tableRow(r));
body('INATIVO: distribuidoras antigas do seed legado foram marcadas como inativas; produtos delas recebem ativo=0, preservando histórico de pedidos.');

subTitle('4.2 enderecos_entrega');
body('Endereços de entrega cadastrados pela empresa compradora. Usados no checkout e referenciados em pedidos.');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL PK'],
  ['empresa_id', 'FK → empresas NOT NULL'],
  ['apelido', 'VARCHAR(80) — ex.: Depósito, Matriz'],
  ['logradouro', 'VARCHAR(150)'],
  ['numero', 'VARCHAR(20)'],
  ['complemento', 'VARCHAR(80)'],
  ['bairro, cidade', 'VARCHAR(80) cada'],
  ['uf', 'VARCHAR(2)'],
  ['cep', 'VARCHAR(9)'],
  ['principal', 'BOOLEAN default false — endereço padrão'],
].forEach((r) => tableRow(r));

subTitle('4.3 formas_pagamento_salvas');
body('Preferências de pagamento por empresa. Não armazena dados sensíveis de cartão — apenas tipo e apelido.');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL PK'],
  ['empresa_id', 'FK → empresas NOT NULL'],
  ['tipo', 'VARCHAR(20) — pix | credito | debito | dinheiro'],
  ['apelido', 'VARCHAR(80) — nome exibido no app'],
  ['principal', 'BOOLEAN default false'],
].forEach((r) => tableRow(r));

subTitle('4.4 cartoes_pagamento_salvos (Fase 5)');
body('Cartões de crédito/débito salvos por empresa. CVV nunca é persistido.');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL PK'],
  ['empresa_id', 'FK → empresas NOT NULL'],
  ['tipo', 'VARCHAR(20) — credito | debito'],
  ['apelido', 'VARCHAR(80) — opcional'],
  ['bandeira', 'VARCHAR(40) — Visa, Master, Elo...'],
  ['ultimos_digitos', 'VARCHAR(4) NOT NULL'],
  ['numero_mascarado', 'VARCHAR(24) — ex.: **** **** **** 1234'],
  ['validade', 'VARCHAR(5) — MM/AA'],
  ['titular', 'VARCHAR(120)'],
  ['criado_em', 'TIMESTAMP'],
].forEach((r) => tableRow(r));

subTitle('4.5 solicitacoes_compra');
body('Pedido B2B principal do marketplace. Um registro por fornecedor no checkout multi-loja.');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL PK'],
  ['empresa_compradora_id', 'FK → empresas NOT NULL'],
  ['empresa_fornecedora_id', 'FK → empresas (distribuidor) NOT NULL'],
  ['usuario_solicitante_id', 'FK → usuarios NOT NULL'],
  ['valor_total', 'NUMERIC(10,2) NOT NULL'],
  ['status', 'VARCHAR(20) — enviada | confirmada | em_rota | entregue | cancelada'],
  ['observacao', 'VARCHAR(500)'],
  ['metodo_pagamento', 'VARCHAR(20)'],
  ['endereco_resumo + campos de endereço', 'Snapshot igual ao pedido — cep, logradouro, etc.'],
  ['taxa_entrega', 'NUMERIC(10,2) default 0'],
  ['pedido_id', 'FK → pedido — vínculo com registro financeiro (Fase 3)'],
  ['estoque_comprador_creditado', 'BOOLEAN default false — controle de idempotência (Fase 4)'],
  ['criado_em', 'TIMESTAMP'],
].forEach((r) => tableRow(r));

subTitle('4.6 itens_solicitacao_compra');
tableRow(['Coluna', 'Descrição'], true);
[
  ['id', 'BIGSERIAL PK'],
  ['solicitacao_id', 'FK → solicitacoes_compra NOT NULL'],
  ['produto_id', 'FK → produtos NOT NULL — produto do fornecedor'],
  ['quantidade', 'NUMERIC(10,3) NOT NULL'],
  ['preco_unitario', 'NUMERIC(10,2) — preço no momento da compra'],
  ['subtotal', 'NUMERIC(10,2) — quantidade × preço'],
].forEach((r) => tableRow(r));

// ── 5. Regras de estoque ───────────────────────────────────────
sectionTitle('5. Regras de negócio — estoque');

body('O estoque em produtos serve tanto ao catálogo do distribuidor (venda B2B) quanto ao estoque interno do comprador (produtos recebidos).');

subTitle('5.1 Fluxo na compra (checkout)');
bullet('Ao confirmar checkout, EstoqueProdutoService.debitarEstoque() subtrai a quantidade do produto do fornecedor.');
bullet('Se estoque insuficiente, a API retorna HTTP 400 com mensagem "Estoque insuficiente".');
bullet('Cada fornecedor na sacola gera uma solicitacao_compra separada e um pedido marketplace vinculado.');

subTitle('5.2 Fluxo na entrega');
bullet('Quando status muda para entregue, creditarCompradorSeNecessario() é chamado.');
bullet('Para cada item: busca produto do comprador com codigo_origem = codigo do fornecedor; se não existir, cria cópia com novo codigo (EMP-{empresaId}-{seq}).');
bullet('Soma a quantidade comprada ao estoque do comprador.');
bullet('Marca estoque_comprador_creditado = true para não repetir o crédito.');

subTitle('5.3 Códigos de produto');
table3(['Código', 'Origem', 'Uso'], true);
[
  ['MKT-CDV-001 … MKT-WL-005', 'ProdutoCatalogoFixRunner', '15 produtos oficiais do marketplace (IDs 1001–1025)'],
  ['LEG-{id}', 'data.sql migração', 'Produtos antigos sem código'],
  ['EMP-{empresaId}-{seq}', 'EstoqueProdutoService', 'Produtos criados no estoque do comprador'],
].forEach((r) => table3(r));

// ── 6. Distribuidoras parceiras ────────────────────────────────
sectionTitle('6. Catálogo oficial — três distribuidoras parceiras');

body('O data.sql e o ProdutoCatalogoFixRunner mantêm exatamente 5 produtos ativos por loja. Demais produtos legados são desativados (ativo=0).');

table3(['Distribuidora', 'CNPJ', 'Produtos (código)'], true);
[
  ['Casa dos Vinhos', '30.001.001/0001-01', 'MKT-CDV-001 a 005 — vinhos'],
  ['Cervejaria Caruaru', '30.002.002/0001-02', 'MKT-CC-001 a 005 — cervejas'],
  ['Whisky Labs', '30.003.003/0001-03', 'MKT-WL-001 a 005 — whiskies e conhaque'],
].forEach((r) => table3(r));

doc.moveDown(0.3);
subTitle('Estoque inicial (fornecedor)');
body('Valores definidos no data.sql e reforçados pelo runner — aplicados apenas se o produto ainda não teve movimentação em itens_solicitacao_compra:');
bullet('Casa dos Vinhos: 300, 200, 150, 100, 80 unidades (por produto)');
bullet('Cervejaria Caruaru: 300, 200, 150, 100, 80 unidades');
bullet('Whisky Labs: 300, 200, 150, 100, 80 unidades');

// ── 7. Seeds ───────────────────────────────────────────────────
sectionTitle('7. Dados iniciais (seeds)');

subTitle('7.1 data.sql — o que executa a cada startup');
bullet('Perfil Admin (se não existir)');
bullet('ALTER TABLE idempotentes: estoque, codigo, codigo_origem em produtos');
bullet('Índice único uk_produtos_codigo');
bullet('estoque_comprador_creditado em solicitacoes_compra');
bullet('Extensão de pedido e pagamentos para marketplace');
bullet('CREATE TABLE cartoes_pagamento_salvos IF NOT EXISTS');
bullet('Desativa empresas legadas (tipo INATIVO) e produtos associados');
bullet('Insere/atualiza 3 distribuidoras parceiras e 15 produtos');
bullet('Define estoque inicial condicional');

subTitle('7.2 Runners Java');
bullet('EnderecoSeedRunner — endereços demo para empresas compradoras');
bullet('FormaPagamentoSeedRunner — PIX, crédito, débito demo por empresa');
bullet('FinanceiroSeedRunner — solicitacoes_compra históricas para gráficos financeiros');
bullet('MarketplaceDataFixRunner — normalização de dados de marketplace');
bullet('ProdutoCatalogoFixRunner — IDs fixos 1001–1025, códigos MKT-*, estoque e imagens oficiais');

// ── 8. Diagrama ────────────────────────────────────────────────
sectionTitle('8. Diagrama de relações');

body('Fluxo marketplace B2B:');
mono('Empresa(COMPRADOR) ──► SolicitacaoCompra ◄── Empresa(DISTRIBUIDOR)');
mono('         │                      │');
mono('         │                      ├── ItensSolicitacaoCompra ──► Produto(fornecedor)');
mono('         │                      ├── Usuario(solicitante)');
mono('         │                      └── Pedido(marketplace) ──► Pagamento');
mono('         │');
mono('         ├── EnderecoEntrega');
mono('         ├── FormaPagamentoSalva');
mono('         ├── CartaoPagamentoSalvo');
mono('         └── Produto(comprador) ← creditado na entrega via codigo_origem');

doc.moveDown(0.5);
body('Fluxo PDV (original):');
mono('Empresa ──► Evento ──► Barraca ──► Pedido(pdv) ──► ItemPedido / Pagamento');
mono('Barraca ──► EstoqueBarraca ──► Produto');

// ── 9. Resumo quantitativo ─────────────────────────────────────
sectionTitle('9. Resumo quantitativo');

tableRow(['Métrica', 'Valor'], true);
[
  ['Tabelas no schema original', '10'],
  ['Tabelas adicionadas (marketplace + cartões)', '5'],
  ['Tabelas estendidas', '4 (empresas, produtos, pedido, pagamentos, solicitacoes_compra)'],
  ['Total de tabelas atuais', '15'],
  ['Entidades JPA (@Entity)', '15'],
  ['Distribuidoras ativas no app', '3'],
  ['Produtos ativos no marketplace', '15 (5 por loja)'],
  ['IDs fixos de produtos marketplace', '1001 a 1025'],
].forEach((r) => tableRow(r));

// ── 10. Glossário de status ────────────────────────────────────
sectionTitle('10. Glossário de valores enumerados');

subTitle('Status de solicitacao_compra');
['enviada — pedido criado no checkout',
 'confirmada — fornecedor aceitou',
 'em_rota — saiu para entrega',
 'entregue — concluído; estoque creditado ao comprador',
 'cancelada — pedido cancelado'].forEach(bullet);

subTitle('Tipo de empresa');
['COMPRADOR — bar, restaurante, revenda que compra no marketplace',
 'DISTRIBUIDOR — fornecedor B2B ativo',
 'PLATAFORMA — operador do sistema (legado)',
 'INATIVO — fornecedor desativado, histórico preservado'].forEach(bullet);

subTitle('Tipo de pedido');
['pdv — venda na barraca de evento',
 'marketplace — compra B2B registrada no checkout mobile'].forEach(bullet);

// ── Rodapé ─────────────────────────────────────────────────────
const range = doc.bufferedPageRange();
for (let i = range.start; i < range.start + range.count; i++) {
  doc.switchToPage(i);
  doc.font('Helvetica').fontSize(8).fillColor(colors.muted)
    .text(
      `QuickStock — Documentação do Banco de Dados · Página ${i + 1} de ${range.count}`,
      doc.page.margins.left,
      doc.page.height - 40,
      { align: 'center', width: doc.page.width - doc.page.margins.left - doc.page.margins.right },
    );
}

doc.end();

doc.on('finish', () => {
  console.log(`PDF gerado: ${outputPath}`);
});
