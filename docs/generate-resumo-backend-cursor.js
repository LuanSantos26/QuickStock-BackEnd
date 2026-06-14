const fs = require('fs');
const path = require('path');
const PDFDocument = require('pdfkit');

const outputPath = path.join(__dirname, 'resumo-backend-cursor-ai.pdf');

const doc = new PDFDocument({
  size: 'A4',
  margins: { top: 52, bottom: 52, left: 52, right: 52 },
  info: {
    Title: 'QuickStock Backend - Resumo Cursor AI',
    Author: 'QuickStock / Cursor AI',
    Subject: 'Alterações no backend desde o início do projeto mobile',
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
  headerBg: '#edf2f7',
  highlight: '#ebf8ff',
};

function ensureSpace(h = 80) {
  if (doc.y > doc.page.height - h) doc.addPage();
}

function hr() {
  doc.moveDown(0.3);
  doc.strokeColor(C.line).lineWidth(0.5)
    .moveTo(doc.page.margins.left, doc.y)
    .lineTo(doc.page.width - doc.page.margins.right, doc.y)
    .stroke();
  doc.moveDown(0.5);
}

function section(text) {
  ensureSpace(100);
  doc.moveDown(0.6);
  doc.font('Helvetica-Bold').fontSize(13).fillColor(C.primary).text(text);
  doc.moveDown(0.2);
  hr();
}

function sub(text) {
  ensureSpace(60);
  doc.font('Helvetica-Bold').fontSize(10.5).fillColor(C.secondary).text(text);
  doc.moveDown(0.15);
}

function p(text, opts = {}) {
  doc.font('Helvetica').fontSize(9.5).fillColor(C.text)
    .text(text, { lineGap: 2.5, ...opts });
  doc.moveDown(0.15);
}

function bullet(text) {
  doc.font('Helvetica').fontSize(9.5).fillColor(C.text)
    .text(`  •  ${text}`, { lineGap: 2, indent: 0 });
}

function api(method, path, desc) {
  doc.font('Helvetica-Bold').fontSize(9).fillColor(C.accent)
    .text(`${method} ${path}`, { continued: true });
  doc.font('Helvetica').fontSize(9).fillColor(C.text)
    .text(` — ${desc}`);
  doc.moveDown(0.1);
}

function fileItem(path, desc) {
  doc.font('Courier').fontSize(8.5).fillColor(C.secondary).text(path, { indent: 8 });
  doc.font('Helvetica').fontSize(9).fillColor(C.muted).text(desc, { indent: 8, lineGap: 1 });
  doc.moveDown(0.15);
}

// ── CAPA ───────────────────────────────────────────────────────
doc.font('Helvetica-Bold').fontSize(22).fillColor(C.primary)
  .text('QuickStock BackEnd', { align: 'center' });
doc.moveDown(0.25);
doc.font('Helvetica-Bold').fontSize(15).fillColor(C.secondary)
  .text('Resumo de tudo acrescentado pelo Cursor AI', { align: 'center' });
doc.moveDown(0.8);
doc.font('Helvetica').fontSize(10).fillColor(C.muted)
  .text(`Projeto: QuickStock-BackEnd (Spring Boot 3.4 · Java 17 · PostgreSQL)`, { align: 'center' });
doc.text(`Gerado em: ${new Date().toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' })}`, { align: 'center' });
doc.moveDown(1.2);

p('Este documento lista todas as funcionalidades, APIs, serviços, entidades, configurações e dados de demonstração adicionados ao backend durante o desenvolvimento integrado com o app mobile QuickStock, com apoio do Cursor AI.');
p('Ponto de partida: commit 8654053 ("funcional database") — schema base com CRUD de eventos, barracas, estoque e pedidos de venda. Tudo abaixo foi acrescentado ou refatorado a partir desse ponto.');

// ── 1. VISÃO GERAL ─────────────────────────────────────────────
section('1. Visão geral das alterações');

p('Commit principal de funcionalidades: 4524146 — "marketplace, pedidos, endereco e formas de pagamento".');
p('Alterações locais adicionais (ainda não commitadas): endpoint Stock do dia, previsão de entrega e timeline de pedidos.');

bullet('~70 arquivos Java novos ou significativamente alterados');
bullet('4 novas entidades JPA + extensão da entidade Empresa');
bullet('7 novos controllers REST (+ refatoração de controllers existentes)');
bullet('12+ novos services');
bullet('25+ novos DTOs');
bullet('4 ApplicationRunners de seed + data.sql com 548 linhas');
bullet('JWT, BCrypt, upload de imagens, CORS global, Swagger/OpenAPI');

// ── 2. AUTENTICAÇÃO E CADASTRO ─────────────────────────────────
section('2. Autenticação e cadastro de conta');

sub('Novos arquivos');
fileItem('service/JwtService.java', 'Geração e validação de tokens JWT (jjwt 0.12.6)');
fileItem('service/CadastroService.java', 'Cadastro unificado empresa + usuário admin com BCrypt');
fileItem('controller/CadastroController.java', 'POST /api/cadastro — registro de nova conta');
fileItem('dto/CadastroContaRequestDTO.java', 'Payload empresa + usuário');
fileItem('dto/LoginResponseDTO.java', 'Resposta de login com token JWT');
fileItem('exception/CadastroException.java', 'Exceção de negócio com HTTP status');
fileItem('exception/GlobalExceptionHandler.java', 'Tratamento centralizado de erros JSON');

sub('Alterações em UsuarioController');
api('POST', '/api/usuarios/login', 'Login com e-mail/senha, retorna JWT');
api('POST', '/api/usuarios/register', 'Registro alternativo de usuário');
api('GET', '/api/usuarios/me', 'Dados do usuário autenticado via token');

sub('Configuração (application.properties)');
bullet('jwt.secret e jwt.expiration-ms (24h)');
bullet('spring-security-crypto (BCrypt) no pom.xml');

// ── 3. MARKETPLACE B2B ─────────────────────────────────────────
section('3. Marketplace B2B (fornecedores e catálogo)');

sub('APIs');
api('GET', '/api/marketplace/fornecedores', 'Lista distribuidoras (tipo DISTRIBUIDOR/PLATAFORMA)');
api('GET', '/api/marketplace/fornecedores/{id}/produtos', 'Produtos de um fornecedor');

sub('Novos arquivos');
fileItem('controller/MarketplaceController.java', 'Endpoints do marketplace');
fileItem('service/MarketplaceService.java', 'Filtra empresas fornecedoras e produtos ativos');
fileItem('dto/FornecedorResponseDTO.java', 'Nome, tipo, descrição, logo, capa');

sub('Alterações em Empresa e Produto');
bullet('Empresa: campos tipo, descricao, logo_url, capa_url, criado_em');
bullet('Produto: campos descricao, imagem_url (suporte a catálogo visual)');
bullet('EmpresaRepository: findByTipoInAndIdNot para listar fornecedoras');

sub('Dados de demonstração — data.sql');
bullet('1 empresa PLATAFORMA (QuickStock Distribuidora)');
bullet('10 distribuidoras fictícias com logo/capa Unsplash');
bullet('Dezenas de produtos (cervejas, refrigerantes, etc.) por fornecedor');
bullet('INSERTs idempotentes executados a cada startup');

sub('MarketplaceDataFixRunner');
bullet('Normaliza tipos de empresa e corrige dados inconsistentes no banco');

// ── 4. PEDIDOS DE COMPRA (SACOLA) ──────────────────────────────
section('4. Pedidos de compra B2B (Sacola / Checkout)');

sub('APIs');
api('GET', '/api/solicitacoes-compra?empresaCompradoraId=', 'Lista pedidos do comprador');
api('POST', '/api/solicitacoes-compra', 'Cria pedido (checkout) — status 201');
api('GET', '/api/solicitacoes-compra/{id}', 'Detalhe do pedido com itens e timeline');

sub('Entidades');
bullet('solicitacoes_compra — comprador, fornecedor, usuário, valor, status, pagamento, endereço snapshot, taxa_entrega');
bullet('itens_solicitacao_compra — produto, quantidade, preco_unitario, subtotal');

sub('SolicitacaoCompraService — regras de negócio');
bullet('Valida fornecedor (DISTRIBUIDOR ou PLATAFORMA)');
bullet('Valida método de pagamento: pix, credito, debito, dinheiro');
bullet('Exige enderecoEntregaId e copia snapshot do endereço no pedido');
bullet('Calcula valor total e subtotais por item');
bullet('Demo de status automático: após 20s → em_rota, após 55s → entregue');
bullet('Retorna timeline de 4 etapas (StatusPedidoDTO) para UI estilo iFood');
bullet('Previsão de entrega calculada (previsaoEntregaMinutos/Label) — alteração local');

sub('DTOs');
fileItem('SolicitacaoCompraRequestDTO.java', 'Itens, fornecedor, pagamento, endereço, taxa');
fileItem('SolicitacaoCompraResponseDTO.java', 'Resposta completa + etapas + previsão');
fileItem('ItemSolicitacaoRequestDTO/ResponseDTO.java', 'Itens do pedido');
fileItem('StatusPedidoDTO.java', 'Etapa da timeline (label, concluída, atual)');

// ── 5. ENDEREÇOS ───────────────────────────────────────────────
section('5. Endereços de entrega');

sub('APIs');
api('GET', '/api/enderecos?empresaId=', 'Lista endereços da empresa');
api('POST', '/api/enderecos', 'Cadastra novo endereço (apelido, CEP, logradouro, etc.)');

sub('Arquivos');
fileItem('entity/EnderecoEntrega.java', 'Tabela enderecos_entrega');
fileItem('service/EnderecoEntregaService.java', 'CRUD + validação de endereço principal');
fileItem('controller/EnderecoEntregaController.java', 'REST controller');
fileItem('config/EnderecoSeedRunner.java', 'Endereços demo para empresas compradoras');

// ── 6. FORMAS DE PAGAMENTO ─────────────────────────────────────
section('6. Formas de pagamento salvas');

sub('APIs');
api('GET', '/api/formas-pagamento?empresaId=', 'Lista formas salvas');
api('POST', '/api/formas-pagamento', 'Salva nova forma (tipo + apelido + principal)');
api('DELETE', '/api/formas-pagamento/{id}', 'Remove forma de pagamento');

sub('Arquivos');
fileItem('entity/FormaPagamentoSalva.java', 'Tabela formas_pagamento_salvas');
fileItem('service/FormaPagamentoSalvaService.java', 'Gerencia formas por empresa');
fileItem('config/FormaPagamentoSeedRunner.java', 'PIX, crédito, débito demo');

p('Nota: não armazena dados sensíveis de cartão — apenas tipo e apelido para seleção no app.');

// ── 7. FINANCEIRO ──────────────────────────────────────────────
section('7. Módulo financeiro (Carteira no app)');

sub('APIs');
api('GET', '/api/financeiro/resumo?empresaCompradoraId=', 'Lucros, compras e pedidos dos últimos 6 meses');
api('GET', '/api/financeiro/stock-dia?empresaCompradoraId=', 'Compras/vendas/lucro do dia + movimentos — NOVO (local)');

sub('FinanceiroService');
bullet('obterResumo: combina dados reais de solicitacoes_compra com séries base demo');
bullet('Gráficos: MesLucroDTO, MesValorDTO, MesPedidosDTO');
bullet('obterStockDia: agrega compras do dia (solicitacoes) e estima vendas (pedidos barraca)');
bullet('Retorna StockDiaDTO + lista MovimentoStockDiaDTO');

sub('DTOs financeiros');
fileItem('FinanceiroResumoDTO.java', 'Totais + séries mensais');
fileItem('StockDiaDTO.java', 'Totais do dia, margem, quantidades — NOVO (local)');
fileItem('MovimentoStockDiaDTO.java', 'Movimento individual compra/venda — NOVO (local)');

sub('FinanceiroSeedRunner');
bullet('Gera solicitacoes_compra históricas (meses anteriores) para alimentar gráficos');

// ── 8. NOTIFICAÇÕES ────────────────────────────────────────────
section('8. Notificações');

api('GET', '/api/notificacoes?empresaCompradoraId=', 'Lista notificações da empresa');

fileItem('service/NotificacaoService.java', 'Notificações de compras, promoções e ofertas');
fileItem('dto/NotificacaoDTO.java', 'Título, mensagem, tipo, data');

bullet('Notificações de status de pedidos B2B baseadas em solicitacoes_compra');
bullet('Notificações promocionais demo de fornecedores');

// ── 9. BARRACAS E ESTOQUE ───────────────────────────────────────
section('9. Barracas e estoque (refatoração)');

sub('BarracaService — lógica consolidada');
bullet('Criação de barraca vinculada a evento e responsável');
bullet('Gestão de estoque integrada (EstoqueBarraca)');
bullet('DTOs dedicados em vez de expor entidades diretamente');

sub('APIs BarracaController (refatorado)');
api('GET', '/api/barracas', 'Lista barracas');
api('POST', '/api/barracas', 'Cria barraca');
api('PUT', '/api/barracas/{id}/estoque', 'Atualiza estoque da barraca');
api('PUT/DELETE', '/api/barracas/{id}', 'Atualiza/remove barraca');

sub('DTOs');
fileItem('BarracaRequestDTO/ResponseDTO.java', 'Request/response de barraca');
fileItem('BarracaEstoqueRequestDTO.java', 'Atualização de estoque');
fileItem('EstoqueItemRequestDTO/ResponseDTO.java', 'Itens de estoque');

// ── 10. PRODUTOS E UPLOAD ──────────────────────────────────────
section('10. Produtos e upload de imagens');

sub('ProdutoController — melhorias');
api('POST', '/api/produtos/upload', 'Upload multipart de imagem (max 5MB)');
api('GET/POST/PUT/DELETE', '/api/produtos', 'CRUD completo com DTOs');

fileItem('service/ProdutoUploadService.java', 'Salva imagem em uploads/produtos/');
fileItem('dto/ProdutoRequestDTO/ResponseDTO.java', 'DTOs tipados para produtos');

sub('Configuração');
bullet('upload.dir=uploads/produtos');
bullet('spring.servlet.multipart.max-file-size=5MB');

// ── 11. INFRAESTRUTURA ──────────────────────────────────────────
section('11. Infraestrutura e configuração');

sub('WebConfig.java');
bullet('CORS global em /api/** para o app mobile (React Native)');

sub('application.properties — novas propriedades');
bullet('UTF-8: encoding SQL, Hikari, servlet');
bullet('spring.sql.init.mode=always + defer-datasource-initialization');
bullet('JWT secret e expiration');
bullet('Upload multipart');

sub('pom.xml — dependências adicionadas');
bullet('spring-security-crypto (BCrypt)');
bullet('jjwt-api, jjwt-impl, jjwt-jackson (JWT)');
bullet('springdoc-openapi-starter-webmvc-ui 2.6.0 (Swagger)');

sub('Swagger / OpenAPI');
bullet('Documentação automática em /swagger-ui.html');
bullet('Commit 567adb2: "Adicionado Swagger para documentação da API"');

sub('Repositórios estendidos');
bullet('EmpresaRepository, PerfilRepository — novos métodos de busca');
bullet('BarracaRepository, EventoRepository — queries adicionais');

// ── 12. MAPA DE ENDPOINTS NOVOS ────────────────────────────────
section('12. Mapa completo — endpoints novos para o app mobile');

const endpoints = [
  ['POST', '/api/cadastro', 'Cadastro empresa + usuário'],
  ['POST', '/api/usuarios/login', 'Login JWT'],
  ['GET', '/api/usuarios/me', 'Perfil autenticado'],
  ['GET', '/api/marketplace/fornecedores', 'Lista fornecedores'],
  ['GET', '/api/marketplace/fornecedores/{id}/produtos', 'Catálogo do fornecedor'],
  ['GET', '/api/solicitacoes-compra', 'Pedidos B2B'],
  ['POST', '/api/solicitacoes-compra', 'Checkout sacola'],
  ['GET', '/api/solicitacoes-compra/{id}', 'Acompanhamento pedido'],
  ['GET', '/api/enderecos', 'Endereços salvos'],
  ['POST', '/api/enderecos', 'Novo endereço'],
  ['GET', '/api/formas-pagamento', 'Formas de pagamento'],
  ['POST', '/api/formas-pagamento', 'Salvar forma'],
  ['DELETE', '/api/formas-pagamento/{id}', 'Remover forma'],
  ['GET', '/api/financeiro/resumo', 'Gráficos carteira'],
  ['GET', '/api/financeiro/stock-dia', 'Stock do dia (local)'],
  ['GET', '/api/notificacoes', 'Notificações'],
  ['POST', '/api/produtos/upload', 'Upload imagem produto'],
];

endpoints.forEach(([m, r, d]) => api(m, r, d));

// ── 13. ALTERAÇÕES LOCAIS (NÃO COMMITADAS) ─────────────────────
section('13. Alterações locais pendentes de commit');

p('Estas mudanças existem no working tree mas ainda não foram commitadas no Git:');

bullet('FinanceiroController + FinanceiroService: endpoint GET /api/financeiro/stock-dia');
bullet('StockDiaDTO e MovimentoStockDiaDTO (novos arquivos)');
bullet('SolicitacaoCompraService: demo de status temporal + previsão de entrega');
bullet('SolicitacaoCompraResponseDTO: campos previsaoEntregaMinutos, previsaoEntregaLabel, etapas');

p('Reinicie o Spring Boot após compilar para que o app mobile use a API completa (sem fallback local).');

// ── 14. RESUMO QUANTITATIVO ────────────────────────────────────
section('14. Resumo quantitativo');

const stats = [
  ['Controllers novos', '7 (Cadastro, Marketplace, SolicitacaoCompra, Endereco, FormaPagamento, Financeiro, Notificacao)'],
  ['Services novos', '12+ (Cadastro, Jwt, Marketplace, SolicitacaoCompra, Endereco, FormaPagamento, Financeiro, Notificacao, Barraca, ProdutoUpload, ...)'],
  ['Entidades novas', '4 (EnderecoEntrega, FormaPagamentoSalva, SolicitacaoCompra, ItemSolicitacaoCompra)'],
  ['DTOs novos', '25+'],
  ['Seeds', 'data.sql + 4 ApplicationRunners'],
  ['Linhas adicionadas (git diff 8654053..HEAD)', '~3.826 linhas'],
  ['Commits de feature', '4524146 (marketplace), 567adb2 (Swagger)'],
];

stats.forEach(([k, v]) => {
  doc.font('Helvetica-Bold').fontSize(9).fillColor(C.secondary).text(`${k}: `, { continued: true });
  doc.font('Helvetica').fontSize(9).fillColor(C.text).text(v);
});

// ── RODAPÉ ─────────────────────────────────────────────────────
const range = doc.bufferedPageRange();
for (let i = range.start; i < range.start + range.count; i++) {
  doc.switchToPage(i);
  doc.font('Helvetica').fontSize(7.5).fillColor(C.muted)
    .text(
      `QuickStock BackEnd · Resumo Cursor AI · Página ${i + 1}/${range.count}`,
      doc.page.margins.left,
      doc.page.height - 36,
      { align: 'center', width: doc.page.width - doc.page.margins.left - doc.page.margins.right },
    );
}

doc.end();

process.stdout.write(`PDF gerado: ${outputPath}\n`);
