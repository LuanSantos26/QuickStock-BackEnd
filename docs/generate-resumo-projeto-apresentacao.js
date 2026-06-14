const fs = require('fs');
const path = require('path');
const PDFDocument = require('pdfkit');

const outputPath = path.join(__dirname, 'resumo-projeto-quickstock-apresentacao.pdf');

const doc = new PDFDocument({
  size: 'A4',
  margins: { top: 52, bottom: 52, left: 52, right: 52 },
  info: {
    Title: 'QuickStock - Resumo Completo do Projeto',
    Author: 'QuickStock Team',
    Subject: 'Apresentação: Mobile + Backend + Banco de Dados',
    CreationDate: new Date(),
  },
});

doc.pipe(fs.createWriteStream(outputPath));

const C = {
  primary: '#1a365d',
  secondary: '#2c5282',
  text: '#1a202c',
  muted: '#4a5568',
  line: '#cbd5e0',
  header: '#edf2f7',
  accent: '#3182ce',
};

function ensure(min = 70) {
  if (doc.y > doc.page.height - min) doc.addPage();
}

function hr() {
  doc.moveDown(0.3);
  doc.strokeColor(C.line).lineWidth(0.5)
    .moveTo(doc.page.margins.left, doc.y)
    .lineTo(doc.page.width - doc.page.margins.right, doc.y).stroke();
  doc.moveDown(0.5);
}

function h1(text) {
  ensure(100);
  doc.moveDown(0.6);
  doc.font('Helvetica-Bold').fontSize(14).fillColor(C.primary).text(text);
  doc.moveDown(0.2);
  hr();
}

function h2(text) {
  ensure(70);
  doc.font('Helvetica-Bold').fontSize(11).fillColor(C.secondary).text(text);
  doc.moveDown(0.2);
}

function p(text, opts = {}) {
  ensure(35);
  doc.font('Helvetica').fontSize(10).fillColor(C.text).text(text, { lineGap: 3, ...opts });
  doc.moveDown(0.15);
}

function bullet(text) {
  ensure(28);
  doc.font('Helvetica').fontSize(10).fillColor(C.text).text(`• ${text}`, { indent: 10, lineGap: 2 });
}

function code(text) {
  ensure(28);
  doc.font('Courier').fontSize(8.5).fillColor(C.text).text(text, { indent: 6, lineGap: 1.5 });
  doc.moveDown(0.15);
}

function tbl(cols, header = false, w = [0.30, 0.70]) {
  const x0 = doc.page.margins.left;
  const tw = doc.page.width - x0 - doc.page.margins.right;
  const cw = w.map((f) => tw * f);
  const y0 = doc.y;
  if (y0 > doc.page.height - 55) doc.addPage();
  if (header) {
    doc.rect(x0, y0, tw, 20).fill(C.header);
    doc.fillColor(C.primary).font('Helvetica-Bold').fontSize(9);
    let x = x0 + 5;
    cols.forEach((c, i) => { doc.text(c, x, y0 + 5, { width: cw[i] - 6 }); x += cw[i]; });
    doc.y = y0 + 24;
  } else {
    doc.font('Helvetica').fontSize(9).fillColor(C.text);
    let x = x0 + 5;
    const startY = doc.y;
    cols.forEach((c, i) => { doc.text(c, x, startY, { width: cw[i] - 6, lineGap: 2 }); x += cw[i]; });
    doc.moveDown(0.3);
    doc.strokeColor(C.line).lineWidth(0.3).moveTo(x0, doc.y).lineTo(x0 + tw, doc.y).stroke();
    doc.moveDown(0.2);
  }
}

function tbl3(cols, header = false) {
  tbl(cols, header, [0.22, 0.30, 0.48]);
}

// ═══ CAPA ═══════════════════════════════════════════════════════
doc.font('Helvetica-Bold').fontSize(28).fillColor(C.primary)
  .text('QuickStock', { align: 'center' });
doc.moveDown(0.2);
doc.font('Helvetica-Bold').fontSize(15).fillColor(C.secondary)
  .text('Resumo Completo do Projeto', { align: 'center' });
doc.moveDown(0.4);
doc.font('Helvetica').fontSize(11).fillColor(C.muted)
  .text('Mobile · Backend · Banco de Dados', { align: 'center' });
doc.moveDown(0.3);
doc.text('Material de estudo para apresentação', { align: 'center' });
doc.moveDown(1);
doc.font('Helvetica').fontSize(10).fillColor(C.muted)
  .text(`Gerado em: ${new Date().toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' })}`, { align: 'center' });

doc.moveDown(2);
p('O QuickStock é um sistema de gestão de estoque e marketplace B2B para empresas do setor de bebidas (bares, restaurantes, revendas). O projeto é composto por um app mobile (React Native/Expo) e uma API REST (Spring Boot/PostgreSQL). Este documento resume arquitetura, modelos conceitual/lógico/físico, principais códigos e fluxos de negócio.');

// ═══ 1. VISÃO GERAL ═════════════════════════════════════════════
h1('1. Visão geral do sistema');

h2('1.1 O que o sistema faz');
bullet('Cadastro e login de empresas compradoras (CNPJ, telefone, usuário admin)');
bullet('Marketplace B2B: navegar fornecedores, ver produtos, adicionar à sacola e finalizar compra');
bullet('Acompanhamento de pedidos estilo delivery (enviada → confirmada → em rota → entregue)');
bullet('Gestão de catálogo próprio: CRUD de produtos com imagem e estoque');
bullet('Barraquinhas/filiais com estoque por ponto de venda (fluxo PDV legado)');
bullet('Dashboard financeiro: resumo de compras/vendas e stock do dia');
bullet('Endereços de entrega, formas de pagamento e cartões salvos');

h2('1.2 Repositórios');
tbl(['Projeto', 'Tecnologia / Path'], true);
[
  ['Mobile (front)', 'React Native 0.83 + Expo 54 — c:\\Dev\\Code\\Faculdade\\Mobile'],
  ['Backend (API)', 'Spring Boot 3.4 + Java 17 — c:\\Dev\\Code\\Faculdade\\QuickStock-BackEnd'],
  ['Banco de dados', 'PostgreSQL — banco quickstock, porta 5432'],
  ['Comunicação', 'HTTP REST JSON — API na porta 8080'],
].forEach((r) => tbl(r));

h2('1.3 Stack tecnológica');
tbl3(['Camada', 'Tecnologia', 'Detalhe'], true);
[
  ['Mobile', 'Expo / RN', 'TypeScript, React Navigation, AsyncStorage, expo-image-picker'],
  ['Backend', 'Spring Boot', 'JPA/Hibernate, Lombok, jjwt, springdoc-openapi'],
  ['Banco', 'PostgreSQL', 'ddl-auto=update + data.sql idempotente'],
  ['Auth', 'JWT manual', 'BCrypt para senha; token 24h; Bearer em /api/usuarios/me'],
  ['Uploads', 'Multipart', 'Imagens em uploads/produtos/ servidas em /uploads/**'],
].forEach((r) => tbl3(r));

// ═══ 2. MODELO CONCEITUAL ═══════════════════════════════════════
h1('2. Modelo conceitual');

p('O modelo conceitual descreve as entidades de negócio e seus relacionamentos, independente de implementação técnica.');

h2('2.1 Entidades principais');
tbl(['Entidade', 'Descrição conceitual'], true);
[
  ['Empresa', 'Organização cadastrada (compradora ou distribuidora). Identificada por CNPJ.'],
  ['Usuário', 'Pessoa que opera o sistema em nome de uma empresa. Possui perfil (Admin, etc.).'],
  ['Produto', 'Item do catálogo (nome, preço, unidade, imagem, estoque, código único).'],
  ['Fornecedor', 'Empresa do tipo DISTRIBUIDOR que vende no marketplace.'],
  ['Solicitação de Compra', 'Pedido B2B: comprador solicita produtos a um fornecedor.'],
  ['Pedido (financeiro)', 'Registro de venda: PDV na barraca ou checkout marketplace.'],
  ['Endereço de Entrega', 'Local onde a mercadoria será recebida.'],
  ['Forma de Pagamento', 'Preferência salva (PIX, crédito, débito, dinheiro).'],
  ['Cartão Salvo', 'Dados mascarados do cartão (sem CVV).'],
  ['Evento / Barraca', 'Contexto de feira/evento com ponto de venda físico (PDV).'],
  ['Estoque Barraca', 'Quantidade de produto disponível em uma barraca específica.'],
].forEach((r) => tbl(r));

h2('2.2 Relacionamentos conceituais');
code('Empresa COMPRADORA ──realiza──► Solicitação de Compra ◄──fornece── Empresa DISTRIBUIDORA');
code('Solicitação de Compra ──contém──► Item (produto + quantidade + preço)');
code('Solicitação de Compra ──gera──► Pedido marketplace + Pagamento');
code('Empresa ──possui──► Produtos (catálogo próprio ou recebidos na entrega)');
code('Empresa ──cadastra──► Endereços, Formas de Pagamento, Cartões');
code('Evento ──contém──► Barracas ──registram──► Pedidos PDV + Estoque');

h2('2.3 Regras de negócio centrais');
bullet('Comprador só vê fornecedores do tipo DISTRIBUIDOR (exceto a própria empresa)');
bullet('Checkout debita estoque do fornecedor imediatamente');
bullet('Na entrega, estoque é creditado ao comprador (produto copiado com codigo_origem)');
bullet('Sacola multi-fornecedor gera uma solicitação por distribuidora + taxa R$ 7/fornecedor');
bullet('Tracking de pedido é simulado por tempo (demo): ~20s em rota, ~55s entregue');
bullet('Dois estoques coexistem: Produto.estoque (empresa) e EstoqueBarraca (PDV por barraca)');

// ═══ 3. MODELO LÓGICO ═══════════════════════════════════════════
h1('3. Modelo lógico');

p('O modelo lógico traduz o conceito em estruturas de dados e contratos de API.');

h2('3.1 Modelo lógico de dados — 15 tabelas');
tbl3(['Grupo', 'Tabelas', 'Função'], true);
[
  ['Autenticação', 'perfis, empresas, usuarios', 'Cadastro, login, perfis de acesso'],
  ['Catálogo', 'produtos', 'Produtos por empresa + estoque + código'],
  ['PDV', 'eventos, barracas, estoque_barraca, pedido, itens_pedido, pagamentos', 'Vendas em evento/feira'],
  ['Marketplace', 'solicitacoes_compra, itens_solicitacao_compra', 'Compras B2B entre empresas'],
  ['Cadastros aux.', 'enderecos_entrega, formas_pagamento_salvas, cartoes_pagamento_salvos', 'Checkout e preferências'],
].forEach((r) => tbl3(r));

h2('3.2 Modelo lógico de API — grupos de endpoints');
tbl(['Prefixo /api/...', 'Responsabilidade'], true);
[
  ['cadastro, usuarios/login, usuarios/me', 'Autenticação e perfil'],
  ['marketplace/fornecedores', 'Listar distribuidoras e catálogo'],
  ['solicitacoes-compra', 'Checkout, histórico, acompanhamento'],
  ['produtos (+ /upload)', 'CRUD catálogo próprio + imagens'],
  ['barracas, estoque-barraca', 'Barraquinhas e estoque PDV'],
  ['enderecos, formas-pagamento, cartoes-pagamento', 'Dados de checkout'],
  ['financeiro/resumo, financeiro/stock-dia', 'Dashboard e carteira'],
  ['pedidos, itens-pedido, pagamentos', 'Fluxo PDV (legado)'],
  ['empresas, eventos, perfis, notificacoes', 'Cadastros e alertas'],
].forEach((r) => tbl(r));

h2('3.3 DTOs e contratos principais (Backend)');
bullet('CadastroContaRequestDTO → POST /api/cadastro (empresa + usuário num payload)');
bullet('LoginRequestDTO → LoginResponseDTO (token JWT + dados do usuário)');
bullet('SolicitacaoCompraRequestDTO → itens[], enderecoEntregaId, metodoPagamento, empresaCompradoraId');
bullet('SolicitacaoCompraResponseDTO → status, previsaoEntrega (calculado), itens, endereço snapshot');
bullet('ProdutoRequestDTO → nome, precoVenda, unidade, estoque, imagemUrl, empresaId');

h2('3.4 Modelo lógico do Mobile — camadas');
code('Screens (UI) → Contexts (estado) → Services (HTTP) → Backend API');
code('Persistência local: AsyncStorage (sessão, carrinho, chaves PIX, endereço selecionado)');

// ═══ 4. MODELO FÍSICO ═══════════════════════════════════════════
h1('4. Modelo físico');

h2('4.1 Infraestrutura');
tbl(['Componente', 'Implementação física'], true);
[
  ['Servidor API', 'JVM local — Spring Boot em localhost:8080'],
  ['Banco', 'PostgreSQL local — jdbc:postgresql://localhost:5432/quickstock'],
  ['App mobile', 'Expo Go ou build nativo — conecta via IP da máquina (não localhost no device)'],
  ['Arquivos estáticos', 'Disco: QuickStock-BackEnd/uploads/ exposto em /uploads/**'],
  ['Sessão mobile', 'AsyncStorage @quickstock_session (token + user + expiração)'],
  ['Carrinho mobile', 'AsyncStorage @quickstock_purchase_cart_{empresaId}'],
].forEach((r) => tbl(r));

h2('4.2 Configuração física do backend (application.properties)');
bullet('spring.jpa.hibernate.ddl-auto=update — schema evolui com entidades JPA');
bullet('spring.sql.init.mode=always — data.sql roda a cada startup');
bullet('jwt.secret + jwt.expiration-ms=86400000 (24 horas)');
bullet('upload.dir=uploads/produtos — limite 10MB por arquivo');

h2('4.3 Configuração física do mobile (api.ts)');
bullet('API_BASE_URL = http://{host}:8080');
bullet('Android emulator: host 10.0.2.2 | Device físico: IP da rede local');
bullet('getImageUrl() prefixa paths relativos (/uploads/...) com a base da API');

h2('4.4 Seeds e inicialização');
bullet('data.sql — migrações idempotentes + 3 distribuidoras + 15 produtos oficiais');
bullet('EnderecoSeedRunner — endereços demo por empresa compradora');
bullet('FormaPagamentoSeedRunner — PIX/crédito/débito/dinheiro demo');
bullet('ProdutoCatalogoFixRunner — IDs fixos 1001–1025, códigos MKT-*');
bullet('MarketplaceDataFixRunner — sincroniza catálogo e desativa legado');

// ═══ 5. BACKEND ═════════════════════════════════════════════════
h1('5. Backend — arquitetura e código principal');

h2('5.1 Estrutura de pacotes');
code('com.quickstock.backend');
code('  controller/  — 18 REST controllers (@RestController, /api/*)');
code('  service/     — 14 services (regras de negócio)');
code('  entity/      — 15 entidades JPA (@Entity)');
code('  repository/  — 15 interfaces Spring Data JPA');
code('  dto/         — 33 DTOs request/response');
code('  config/      — WebConfig (CORS, uploads) + ApplicationRunners');
code('  exception/   — CadastroException + GlobalExceptionHandler');

h2('5.2 Controllers mais importantes');
tbl3(['Controller', 'Path base', 'Função'], true);
[
  ['CadastroController', '/api/cadastro', 'Registro empresa + admin'],
  ['UsuarioController', '/api/usuarios', 'Login, /me (JWT), CRUD'],
  ['MarketplaceController', '/api/marketplace', 'Fornecedores e produtos'],
  ['SolicitacaoCompraController', '/api/solicitacoes-compra', 'Checkout B2B'],
  ['ProdutoController', '/api/produtos', 'CRUD + upload imagem'],
  ['FinanceiroController', '/api/financeiro', 'Resumo e stock-dia'],
  ['BarracaController', '/api/barracas', 'Barraquinhas + estoque'],
  ['EnderecoEntregaController', '/api/enderecos', 'Endereços + ViaCEP'],
].forEach((r) => tbl3(r));

h2('5.3 Services — métodos críticos');

p('CadastroService.cadastrarConta(dto)');
bullet('Cria Empresa (tipo COMPRADOR) + Usuario com perfil Admin + senha BCrypt');

p('JwtService');
bullet('generateToken(Usuario) — HMAC-SHA, claim sub=userId');
bullet('isTokenValid(token), getUserId(token) — usado em GET /api/usuarios/me');

p('SolicitacaoCompraService.criar(dto) — coração do checkout');
bullet('1. Valida comprador, fornecedor DISTRIBUIDOR, usuário, pagamento');
bullet('2. Valida enderecoEntregaId pertence à empresa compradora');
bullet('3. Para cada item: EstoqueProdutoService.debitarEstoque(produto, qtd)');
bullet('4. Cria SolicitacaoCompra (status aguardando_liberacao)');
bullet('5. criarPedidoMarketplace() → Pedido tipo=marketplace + ItemPedido + Pagamento');
bullet('6. Vincula solicitacao.pedido');

p('SolicitacaoCompraService.obterPorId / listarPorComprador');
bullet('atualizarStatusDemonstracao() — avança status por tempo (demo)');
bullet('Se entregue → EstoqueProdutoService.creditarCompradorSeNecessario()');

p('EstoqueProdutoService');
bullet('debitarEstoque(produto, qtd) — subtrai estoque do fornecedor; erro 400 se insuficiente');
bullet('creditarCompradorSeNecessario(solicitacao) — copia produtos para catálogo do comprador');
bullet('gerarCodigoCatalogo(empresaId) — gera EMP-{id}-{seq}');

p('MarketplaceService');
bullet('listarFornecedores(empresaCompradoraId) — tipo DISTRIBUIDOR, exclui própria empresa');
bullet('listarProdutosFornecedor(id) — produtos ativos com estoque');

p('FinanceiroService');
bullet('obterResumo(empresaId) — compras (solicitações) vs vendas (pedidos PDV) 6 meses');
bullet('obterStockDia(empresaId) — movimentos do dia + margem estimada');

h2('5.4 Entidades JPA principais');
tbl(['Entidade', 'Tabela', 'Destaque'], true);
[
  ['Empresa', 'empresas', 'tipo: COMPRADOR | DISTRIBUIDOR | PLATAFORMA | INATIVO'],
  ['Produto', 'produtos', 'codigo UNIQUE, codigo_origem, estoque NUMERIC(10,3)'],
  ['SolicitacaoCompra', 'solicitacoes_compra', 'Snapshot endereço + pedido_id + estoque_comprador_creditado'],
  ['Pedido', 'pedido', 'tipo pdv|marketplace; barraca_id nullable'],
  ['CartaoPagamentoSalvo', 'cartoes_pagamento_salvos', 'Dados mascarados, sem CVV'],
].forEach((r) => tbl(r));

h2('5.5 Segurança');
bullet('BCrypt para hash de senha (spring-security-crypto)');
bullet('JWT com secret em application.properties — expiração 24h');
bullet('Único endpoint que valida Bearer token: GET /api/usuarios/me');
bullet('Demais endpoints validam empresaId/usuarioId nos parâmetros (modelo acadêmico/demo)');
bullet('CORS liberado para * em WebConfig');

// ═══ 6. FRONTEND MOBILE ═══════════════════════════════════════════
h1('6. Frontend Mobile — arquitetura e código principal');

h2('6.1 Estrutura de pastas (src/)');
tbl(['Pasta', 'Conteúdo'], true);
[
  ['screens/', 'Telas: Home, Cart (marketplace), Sacola, Login, Register, etc.'],
  ['components/', 'UI reutilizável: BottomTabBar, ProductCard, modais, headers'],
  ['context/', 'Estado global: Auth, PurchaseCart, Products, Barraquinhas'],
  ['services/', 'Chamadas HTTP e AsyncStorage'],
  ['config/', 'api.ts — base URL e getImageUrl'],
  ['theme/', 'theme.ts — cores, spacing, fonts'],
  ['utils/', 'safeArea, pixUtils, cep, imagens, datas'],
].forEach((r) => tbl(r));

h2('6.2 Navegação (App.tsx)');
p('Dois stacks controlados por AuthContext:');
bullet('GuestNavigator: Welcome → Register | Login');
bullet('AuthenticatedNavigator: Home, Barraquinhas, Cart, Sacola, FormasPagamento, Cards, + telas secundárias');
bullet('Tab bar customizada (BottomTabBar.tsx) — não usa @react-navigation/bottom-tabs');
bullet('Botão central "+" → ManageProductsScreen (AddItem)');

h2('6.3 Contextos — estado global');
tbl3(['Context', 'Hook', 'Responsabilidade'], true);
[
  ['AuthContext', 'useAuth()', 'user, token, signIn, signOut, updateUser — AsyncStorage'],
  ['PurchaseCartContext', 'usePurchaseCart()', 'Sacola: addItem, grupos por fornecedor, taxa R$7/fornecedor'],
  ['ProductsContext', 'useProdutos()', 'Catálogo próprio da empresa — refresh após entrega'],
  ['BarraquinhasContext', 'useBarraquinhas()', 'CRUD barraquinhas'],
  ['ConfirmDialogContext', 'useConfirmDialog()', 'Modal de confirmação reutilizável'],
].forEach((r) => tbl3(r));

h2('6.4 Services — integração com API');
tbl3(['Service', 'Endpoints principais', 'Uso'], true);
[
  ['authService', 'POST /cadastro, /usuarios/login, GET /me', 'Login e registro'],
  ['marketplaceService', 'GET fornecedores, POST solicitacoes-compra', 'Marketplace e checkout'],
  ['productService', 'CRUD /produtos, POST /upload', 'Gestão catálogo próprio'],
  ['enderecoService', 'GET/POST /enderecos, GET /cep/{cep}', 'Endereços na sacola'],
  ['formaPagamentoService', 'GET/POST /formas-pagamento', 'Tipos de pagamento'],
  ['cartaoPagamentoService', 'GET/POST /cartoes-pagamento', 'Cartões mascarados'],
  ['financeiroService', 'GET /financeiro/resumo, /stock-dia', 'Home e Carteira'],
  ['barracaService', 'CRUD /barracas, PUT /estoque', 'Barraquinhas'],
  ['pixChaveService', 'AsyncStorage local', 'Chaves PIX (não vai ao backend)'],
  ['purchaseCartStorage', 'AsyncStorage por empresaId', 'Persistência da sacola'],
].forEach((r) => tbl3(r));

h2('6.5 Telas principais e funções-chave');
tbl3(['Tela / Arquivo', 'Função', 'Código importante'], true);
[
  ['WelcomeScreen', 'Landing login/cadastro', 'Navega para Login ou Register'],
  ['RegisterScreen', 'Cadastro unificado', 'cadastrarConta() — formata CNPJ/telefone'],
  ['Login.tsx', 'Autenticação', 'signIn() → authService.login()'],
  ['HomeScreen', 'Dashboard', 'ProductStockCard, FinancialDonutChart, buscarResumoFinanceiro'],
  ['CartScreen', 'Marketplace (NÃO é carrinho)', 'listarFornecedores, pedidos recentes'],
  ['StoreVitrineScreen', 'Vitrine fornecedor', 'listarProdutosFornecedor → ProductDetail'],
  ['ProductDetailScreen', 'Detalhe + add sacola', 'addItem() do PurchaseCartContext'],
  ['SacolaScreen', 'Carrinho real + checkout', 'processarCheckout() → criarSolicitacaoCompra por fornecedor'],
  ['PedidoAcompanhamentoScreen', 'Tracking pedido', 'buscarSolicitacao() polling 5s'],
  ['ManageProductsScreen', 'CRUD produtos', 'ProductFormModal + uploadImagemProduto'],
  ['BarraquinhasScreen', 'CRUD barraquinhas', 'BarracaFormModal + sincronizar estoque'],
  ['CardsScreen', 'Carteira financeira', 'buscarStockDia, buscarResumoFinanceiro'],
  ['FormasPagamentoScreen', 'Pagamentos salvos', 'Modais PIX e cartão'],
  ['EnderecosScreen', 'Endereços entrega', 'EnderecoFormModal + consultarCep'],
  ['ConfiguracoesScreen', 'Perfil e empresa', 'atualizarUsuario, atualizarEmpresa'],
].forEach((r) => tbl3(r));

h2('6.6 Componentes reutilizáveis importantes');
bullet('BottomTabBar — navegação inferior com badge na Sacola');
bullet('TabScreenLayout — layout padrão com safe area e tab bar');
bullet('ProductCard / ProductStockCard — card de produto com imagem, preço, estoque');
bullet('CheckoutPaymentModal — modal PIX/cartão/dinheiro no checkout');
bullet('ProductFormModal — formulário criar/editar produto com picker de imagem');
bullet('ScreenTopGradient — gradiente amarelo (#F8B125) no topo das telas autenticadas');

h2('6.7 Tema visual');
bullet('Cor principal autenticado: #F8B125 (amarelo/dourado)');
bullet('Telas auth: gradiente azul → laranja (Background.tsx)');
bullet('Tokens em theme.ts: primaryYellow, primaryBlue, SPACING, FONTS');
bullet('Safe area Android: useBottomInset() mínimo 48px (utils/safeArea.ts)');

// ═══ 7. BANCO DE DADOS (RESUMO) ═════════════════════════════════
h1('7. Banco de dados — resumo');

h2('7.1 Evolução do schema');
tbl(['Fase', 'Alteração'], true);
[
  ['Original', '10 tabelas: PDV, eventos, barracas, produtos, usuários'],
  ['Marketplace', '+4 tabelas: endereços, formas pagamento, solicitações + itens; empresas.tipo'],
  ['Checkout', 'pedido estendido (tipo marketplace); pagamentos.referencia_pagamento'],
  ['Estoque', 'produtos.estoque, codigo, codigo_origem; solicitacoes.estoque_comprador_creditado'],
  ['Cartões', 'cartoes_pagamento_salvos; 3 distribuidoras parceiras; legado INATIVO'],
].forEach((r) => tbl(r));

h2('7.2 Distribuidoras ativas no marketplace');
tbl3(['Loja', 'CNPJ', 'Produtos (códigos)'], true);
[
  ['Casa dos Vinhos', '30.001.001/0001-01', 'MKT-CDV-001 a 005 (IDs 1001–1005)'],
  ['Cervejaria Caruaru', '30.002.002/0001-02', 'MKT-CC-001 a 005 (IDs 1011–1015)'],
  ['Whisky Labs', '30.003.003/0001-03', 'MKT-WL-001 a 005 (IDs 1021–1025)'],
].forEach((r) => tbl3(r));

h2('7.3 Status de solicitação de compra');
bullet('enviada / aguardando_liberacao → confirmada → em_rota → entregue | cancelada');
bullet('Na entregue: credita estoque ao comprador via EstoqueProdutoService');

// ═══ 8. FLUXOS END-TO-END ═════════════════════════════════════════
h1('8. Fluxos principais (ponta a ponta)');

h2('8.1 Cadastro e login');
code('Mobile: RegisterScreen → POST /api/cadastro → CadastroService.cadastrarConta');
code('  → cria empresas + usuarios (BCrypt) → usuário faz login manual');
code('Mobile: Login → POST /api/usuarios/login → JWT → AsyncStorage → AuthContext');
code('Sessão: GET /api/usuarios/me com Bearer token valida expiração');

h2('8.2 Compra no marketplace (fluxo completo)');
code('1. CartScreen → GET /marketplace/fornecedores');
code('2. StoreVitrineScreen → GET /marketplace/fornecedores/{id}/produtos');
code('3. ProductDetailScreen → PurchaseCartContext.addItem() → AsyncStorage');
code('4. SacolaScreen → agrupa por fornecedor, seleciona endereço e pagamento');
code('5. CheckoutPaymentModal → processarCheckout()');
code('   → para cada fornecedor: POST /solicitacoes-compra');
code('   → Backend: debitarEstoque + criar SolicitacaoCompra + Pedido marketplace');
code('6. PedidoAcompanhamentoScreen → GET /solicitacoes-compra/{id} (polling 5s)');
code('7. Status entregue → creditarCompradorSeNecessario → refreshProdutos() na Home');

h2('8.3 Gestão de produtos próprios');
code('ManageProductsScreen → ProductsContext → GET /api/produtos?empresaId=');
code('ProductFormModal → POST/PUT /api/produtos + POST /api/produtos/upload');
code('ProdutoController → EstoqueProdutoService.gerarCodigoCatalogo() se novo');

h2('8.4 Barraquinhas (PDV)');
code('BarraquinhasScreen → POST /api/barracas → BarracaService.criar');
code('  → cria Evento "Operação principal" se necessário');
code('PUT /api/barracas/{id}/estoque → sincroniza EstoqueBarraca');

h2('8.5 Dashboard financeiro');
code('HomeScreen / CardsScreen → GET /api/financeiro/resumo');
code('  → FinanceiroService agrega SolicitacaoCompra (compras) vs Pedido PDV (vendas)');
code('GET /api/financeiro/stock-dia → movimentos e margem do dia');

// ═══ 9. DIAGRAMA INTEGRADO ════════════════════════════════════════
h1('9. Diagrama integrado Mobile ↔ API ↔ Banco');

code('┌─────────────┐    HTTP/JSON     ┌──────────────────┐    JPA     ┌────────────┐');
code('│  App Mobile │ ◄──────────────► │  Spring Boot API │ ◄────────► │ PostgreSQL │');
code('│  (Expo/RN)  │   :8080          │  18 controllers  │   :5432    │ quickstock │');
code('└─────────────┘                  └──────────────────┘            └────────────┘');
code('      │                                   │');
code(' AsyncStorage                     uploads/ (imagens)');
code(' sessão, sacola, PIX local');

h2('9.1 Camadas por responsabilidade');
tbl3(['Camada', 'Mobile', 'Backend'], true);
[
  ['Apresentação', 'screens/ + components/', 'controllers/ (REST)'],
  ['Estado/Regra', 'context/ + utils/', 'service/'],
  ['Dados', 'services/ + AsyncStorage', 'repository/ + entity/'],
  ['Persistência', 'AsyncStorage (local)', 'PostgreSQL + uploads/'],
].forEach((r) => tbl3(r));

// ═══ 10. GLOSSÁRIO E DICAS PARA APRESENTAÇÃO ═════════════════════
h1('10. Glossário e dicas para apresentação');

h2('10.1 Termos importantes');
tbl(['Termo', 'Significado'], true);
[
  ['Marketplace B2B', 'Compra entre empresas (revenda compra de distribuidor)'],
  ['Sacola', 'Carrinho de compras multi-fornecedor no mobile'],
  ['Cart (tab)', 'Tela de marketplace — listagem de fornecedores (não é carrinho)'],
  ['Solicitação de Compra', 'Entidade principal do pedido B2B no banco'],
  ['codigo_origem', 'Rastreia produto recebido do fornecedor no catálogo do comprador'],
  ['PDV', 'Ponto de venda — vendas em barraca/evento (fluxo original)'],
  ['Snapshot endereço', 'Cópia do endereço gravada no pedido (não muda se endereço for editado)'],
].forEach((r) => tbl(r));

h2('10.2 Pontos fortes para destacar na apresentação');
bullet('Arquitetura em camadas clara: Mobile → API REST → PostgreSQL');
bullet('Dois fluxos de negócio coexistem: marketplace B2B + PDV em eventos');
bullet('Controle de estoque bidirecional: débito na compra, crédito na entrega');
bullet('Checkout multi-fornecedor com taxa de entrega por loja');
bullet('UX completa: cadastro, marketplace, sacola, pagamento, tracking, dashboard');
bullet('Persistência local inteligente (carrinho sobrevive ao fechar app)');

h2('10.3 Limitações conhecidas (transparência acadêmica)');
bullet('JWT validado apenas em /api/usuarios/me — demais rotas confiam no cliente');
bullet('Tracking de entrega simulado por timer (não há integração logística real)');
bullet('Chaves PIX ficam só no dispositivo (AsyncStorage), não no backend');
bullet('Schema evolui via Hibernate update (sem Flyway/Liquibase versionado)');

h2('10.4 Como demonstrar ao vivo');
bullet('1. Registrar empresa → login → Home com gráfico financeiro');
bullet('2. Tab Cart → escolher fornecedor → adicionar produtos à sacola');
bullet('3. Sacola → endereço + pagamento → confirmar → acompanhar pedido');
bullet('4. Após entrega: ver produto creditado em "Meus produtos" (+)');
bullet('5. Mostrar Swagger UI em http://localhost:8080/swagger-ui.html');

// ═══ RODAPÉ ═══════════════════════════════════════════════════════
const range = doc.bufferedPageRange();
for (let i = range.start; i < range.start + range.count; i++) {
  doc.switchToPage(i);
  doc.font('Helvetica').fontSize(8).fillColor(C.muted)
    .text(
      `QuickStock — Resumo do Projeto · Página ${i + 1} de ${range.count}`,
      doc.page.margins.left, doc.page.height - 36,
      { align: 'center', width: doc.page.width - doc.page.margins.left - doc.page.margins.right },
    );
}

doc.end();
doc.on('finish', () => console.log(`PDF gerado: ${outputPath}`));
