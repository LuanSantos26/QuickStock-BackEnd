INSERT INTO perfis (nome, descricao)
SELECT 'Admin', 'Administrador da empresa'
WHERE NOT EXISTS (SELECT 1 FROM perfis WHERE LOWER(nome) = 'admin');

-- ── Coluna de estoque (migração idempotente) ─────────────────────────────────
ALTER TABLE produtos ADD COLUMN IF NOT EXISTS estoque NUMERIC(10, 3);
UPDATE produtos SET estoque = 0 WHERE estoque IS NULL;

-- ── Desativa catálogo legado (mantém histórico de pedidos) ───────────────────
UPDATE empresas SET tipo = 'INATIVO'
WHERE tipo IN ('DISTRIBUIDOR', 'PLATAFORMA')
  AND cnpj NOT IN ('30.001.001/0001-01', '30.002.002/0001-02', '30.003.003/0001-03');

UPDATE produtos SET ativo = 0
WHERE empresa_id IN (SELECT id FROM empresas WHERE tipo = 'INATIVO');

-- ── 3 Distribuidoras parceiras ───────────────────────────────────────────────
INSERT INTO empresas (nome, cnpj, telefone, tipo, descricao, logo_url, capa_url)
SELECT 'Casa dos Vinhos', '30.001.001/0001-01', '81999001001', 'DISTRIBUIDOR',
       'Vinhos nacionais e importados selecionados para bares, restaurantes e revenda.',
       'https://ui-avatars.com/api/?name=CV&background=722F37&color=fff&bold=true&size=200&format=png',
       'https://picsum.photos/seed/casa-dos-vinhos/800/400'
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '30.001.001/0001-01');

INSERT INTO empresas (nome, cnpj, telefone, tipo, descricao, logo_url, capa_url)
SELECT 'Cervejaria Caruaru', '30.002.002/0001-02', '81999002002', 'DISTRIBUIDOR',
       'Cervejas geladas e importadas para revenda em Caruaru e região.',
       'https://ui-avatars.com/api/?name=CC&background=F8B125&color=fff&bold=true&size=200&format=png',
       'https://picsum.photos/seed/cervejaria-caruaru/800/400'
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '30.002.002/0001-02');

INSERT INTO empresas (nome, cnpj, telefone, tipo, descricao, logo_url, capa_url)
SELECT 'Whisky Labs', '30.003.003/0001-03', '81999003003', 'DISTRIBUIDOR',
       'Whiskies, conhaques e destilados premium para colecionadores e revenda.',
       'https://ui-avatars.com/api/?name=WL&background=1a365d&color=fff&bold=true&size=200&format=png',
       'https://picsum.photos/seed/whisky-labs/800/400'
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '30.003.003/0001-03');

UPDATE empresas SET
  nome = 'Casa dos Vinhos',
  tipo = 'DISTRIBUIDOR',
  telefone = '81999001001',
  descricao = 'Vinhos nacionais e importados selecionados para bares, restaurantes e revenda.',
  logo_url = 'https://ui-avatars.com/api/?name=CV&background=722F37&color=fff&bold=true&size=200&format=png',
  capa_url = 'https://picsum.photos/seed/casa-dos-vinhos/800/400'
WHERE cnpj = '30.001.001/0001-01';

UPDATE empresas SET
  nome = 'Cervejaria Caruaru',
  tipo = 'DISTRIBUIDOR',
  telefone = '81999002002',
  descricao = 'Cervejas geladas e importadas para revenda em Caruaru e região.',
  logo_url = 'https://ui-avatars.com/api/?name=CC&background=F8B125&color=fff&bold=true&size=200&format=png',
  capa_url = 'https://picsum.photos/seed/cervejaria-caruaru/800/400'
WHERE cnpj = '30.002.002/0001-02';

UPDATE empresas SET
  nome = 'Whisky Labs',
  tipo = 'DISTRIBUIDOR',
  telefone = '81999003003',
  descricao = 'Whiskies, conhaques e destilados premium para colecionadores e revenda.',
  logo_url = 'https://ui-avatars.com/api/?name=WL&background=1a365d&color=fff&bold=true&size=200&format=png',
  capa_url = 'https://picsum.photos/seed/whisky-labs/800/400'
WHERE cnpj = '30.003.003/0001-03';

-- ── Casa dos Vinhos — produtos ───────────────────────────────────────────────
INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Vinho Mazzei Zisola Sicilia Noto Rosso 750ml', 156.00, 'UN',
       'Vinho tinto siciliano Noto Rosso, garrafa 750 ml.',
       'https://picsum.photos/seed/vinho-mazzei/400/400', 1
FROM empresas e WHERE e.cnpj = '30.001.001/0001-01'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Vinho Mazzei Zisola Sicilia Noto Rosso 750ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Vinho Louis Latour Bourgogne Chardonnay 750 ml', 200.00, 'UN',
       'Vinho branco Bourgogne Chardonnay, garrafa 750 ml.',
       'https://picsum.photos/seed/vinho-latour/400/400', 1
FROM empresas e WHERE e.cnpj = '30.001.001/0001-01'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Vinho Louis Latour Bourgogne Chardonnay 750 ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Vinho Naturelle Tinto Reserva 750 ML', 60.00, 'UN',
       'Vinho tinto reserva Naturelle, garrafa 750 ml.',
       'https://picsum.photos/seed/vinho-naturelle/400/400', 1
FROM empresas e WHERE e.cnpj = '30.001.001/0001-01'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Vinho Naturelle Tinto Reserva 750 ML');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Vinho Carmin De Peumo Carmenere 750 ml', 666.00, 'UN',
       'Vinho tinto Carmenere Carmin De Peumo, garrafa 750 ml.',
       'https://picsum.photos/seed/vinho-carmin/400/400', 1
FROM empresas e WHERE e.cnpj = '30.001.001/0001-01'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Vinho Carmin De Peumo Carmenere 750 ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Vinho Carolina Reserva Sauvignon Blanc 750 ml', 60.00, 'UN',
       'Vinho branco Sauvignon Blanc reserva Carolina, garrafa 750 ml.',
       'https://picsum.photos/seed/vinho-carolina/400/400', 1
FROM empresas e WHERE e.cnpj = '30.001.001/0001-01'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Vinho Carolina Reserva Sauvignon Blanc 750 ml');

UPDATE produtos SET preco_venda = 156.00, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/vinho-mazzei/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.001.001/0001-01'
  AND produtos.nome = 'Vinho Mazzei Zisola Sicilia Noto Rosso 750ml';

UPDATE produtos SET preco_venda = 200.00, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/vinho-latour/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.001.001/0001-01'
  AND produtos.nome = 'Vinho Louis Latour Bourgogne Chardonnay 750 ml';

UPDATE produtos SET preco_venda = 60.00, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/vinho-naturelle/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.001.001/0001-01'
  AND produtos.nome = 'Vinho Naturelle Tinto Reserva 750 ML';

UPDATE produtos SET preco_venda = 666.00, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/vinho-carmin/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.001.001/0001-01'
  AND produtos.nome = 'Vinho Carmin De Peumo Carmenere 750 ml';

UPDATE produtos SET preco_venda = 60.00, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/vinho-carolina/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.001.001/0001-01'
  AND produtos.nome = 'Vinho Carolina Reserva Sauvignon Blanc 750 ml';

-- ── Cervejaria Caruaru — produtos ────────────────────────────────────────────
INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Cerveja Heineken Long Neck 330ml', 5.99, 'UN',
       'Cerveja Heineken long neck 330 ml.',
       'https://picsum.photos/seed/cerveja-heineken/400/400', 1
FROM empresas e WHERE e.cnpj = '30.002.002/0001-02'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Cerveja Heineken Long Neck 330ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Cerveja Corona Long Neck', 6.99, 'UN',
       'Cerveja Corona Extra long neck 330 ml.',
       'https://picsum.photos/seed/cerveja-corona/400/400', 1
FROM empresas e WHERE e.cnpj = '30.002.002/0001-02'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Cerveja Corona Long Neck');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Cerveja Amstel Lata 269ml', 3.19, 'UN',
       'Cerveja Amstel lata 269 ml.',
       'https://picsum.photos/seed/cerveja-amstel/400/400', 1
FROM empresas e WHERE e.cnpj = '30.002.002/0001-02'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Cerveja Amstel Lata 269ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Cerveja Cerpa Export Long Neck 350ml', 8.39, 'UN',
       'Cerveja Cerpa Export long neck 350 ml.',
       'https://picsum.photos/seed/cerveja-cerpa/400/400', 1
FROM empresas e WHERE e.cnpj = '30.002.002/0001-02'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Cerveja Cerpa Export Long Neck 350ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Cerveja Paulaner Munchen Weissbier 500ml', 11.19, 'UN',
       'Cerveja Paulaner Munchen Weissbier garrafa 500 ml.',
       'https://picsum.photos/seed/cerveja-paulaner/400/400', 1
FROM empresas e WHERE e.cnpj = '30.002.002/0001-02'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Cerveja Paulaner Munchen Weissbier 500ml');

UPDATE produtos SET preco_venda = 5.99, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/cerveja-heineken/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.002.002/0001-02'
  AND produtos.nome = 'Cerveja Heineken Long Neck 330ml';

UPDATE produtos SET preco_venda = 6.99, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/cerveja-corona/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.002.002/0001-02'
  AND produtos.nome = 'Cerveja Corona Long Neck';

UPDATE produtos SET preco_venda = 3.19, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/cerveja-amstel/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.002.002/0001-02'
  AND produtos.nome = 'Cerveja Amstel Lata 269ml';

UPDATE produtos SET preco_venda = 8.39, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/cerveja-cerpa/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.002.002/0001-02'
  AND produtos.nome = 'Cerveja Cerpa Export Long Neck 350ml';

UPDATE produtos SET preco_venda = 11.19, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/cerveja-paulaner/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.002.002/0001-02'
  AND produtos.nome = 'Cerveja Paulaner Munchen Weissbier 500ml';

-- ── Whisky Labs — produtos ───────────────────────────────────────────────────
INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Whisky Escocês Royal Salute 21 Anos The Signature Blend 700ml', 989.00, 'UN',
       'Whisky escocês Royal Salute 21 anos, garrafa 700 ml.',
       'https://picsum.photos/seed/whisky-royal-salute/400/400', 1
FROM empresas e WHERE e.cnpj = '30.003.003/0001-03'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Whisky Escocês Royal Salute 21 Anos The Signature Blend 700ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Whisky The Balvenie PortWood 21 Anos 700ml Single Malt Escocês', 2795.30, 'UN',
       'Whisky single malt The Balvenie PortWood 21 anos, garrafa 700 ml.',
       'https://picsum.photos/seed/whisky-balvenie/400/400', 1
FROM empresas e WHERE e.cnpj = '30.003.003/0001-03'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Whisky The Balvenie PortWood 21 Anos 700ml Single Malt Escocês');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Whisky Glenfiddich Single Malt 12 Anos 750ml Escocês', 415.75, 'UN',
       'Whisky single malt Glenfiddich 12 anos, garrafa 750 ml.',
       'https://picsum.photos/seed/whisky-glenfiddich/400/400', 1
FROM empresas e WHERE e.cnpj = '30.003.003/0001-03'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Whisky Glenfiddich Single Malt 12 Anos 750ml Escocês');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Conhaque Martell L''Or de Jean Martell 700ml', 18711.00, 'UN',
       'Conhaque Martell L''Or de Jean Martell, garrafa 700 ml.',
       'https://picsum.photos/seed/conhaque-martell/400/400', 1
FROM empresas e WHERE e.cnpj = '30.003.003/0001-03'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Conhaque Martell L''Or de Jean Martell 700ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Whisky Suntory Hibiki Japanese Harmony 700ml | Blended Premium Japonês', 729.90, 'UN',
       'Whisky japonês Suntory Hibiki Japanese Harmony, garrafa 700 ml.',
       'https://picsum.photos/seed/whisky-hibiki/400/400', 1
FROM empresas e WHERE e.cnpj = '30.003.003/0001-03'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Whisky Suntory Hibiki Japanese Harmony 700ml | Blended Premium Japonês');

UPDATE produtos SET preco_venda = 989.00, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/whisky-royal-salute/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.003.003/0001-03'
  AND produtos.nome = 'Whisky Escocês Royal Salute 21 Anos The Signature Blend 700ml';

UPDATE produtos SET preco_venda = 2795.30, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/whisky-balvenie/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.003.003/0001-03'
  AND produtos.nome = 'Whisky The Balvenie PortWood 21 Anos 700ml Single Malt Escocês';

UPDATE produtos SET preco_venda = 415.75, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/whisky-glenfiddich/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.003.003/0001-03'
  AND produtos.nome = 'Whisky Glenfiddich Single Malt 12 Anos 750ml Escocês';

UPDATE produtos SET preco_venda = 18711.00, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/conhaque-martell/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.003.003/0001-03'
  AND produtos.nome = 'Conhaque Martell L''Or de Jean Martell 700ml';

UPDATE produtos SET preco_venda = 729.90, ativo = 1,
  imagem_url = 'https://picsum.photos/seed/whisky-hibiki/400/400'
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.003.003/0001-03'
  AND produtos.nome = 'Whisky Suntory Hibiki Japanese Harmony 700ml | Blended Premium Japonês';

-- ── Estoque inicial dos parceiros (somente quando ainda não movimentado) ─────
UPDATE produtos SET estoque = 300
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.001.001/0001-01'
  AND produtos.nome = 'Vinho Naturelle Tinto Reserva 750 ML'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));

UPDATE produtos SET estoque = 200
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.001.001/0001-01'
  AND produtos.nome = 'Vinho Carolina Reserva Sauvignon Blanc 750 ml'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));

UPDATE produtos SET estoque = 150
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.001.001/0001-01'
  AND produtos.nome = 'Vinho Mazzei Zisola Sicilia Noto Rosso 750ml'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));

UPDATE produtos SET estoque = 100
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.001.001/0001-01'
  AND produtos.nome = 'Vinho Louis Latour Bourgogne Chardonnay 750 ml'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));

UPDATE produtos SET estoque = 80
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.001.001/0001-01'
  AND produtos.nome = 'Vinho Carmin De Peumo Carmenere 750 ml'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));

UPDATE produtos SET estoque = 300
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.002.002/0001-02'
  AND produtos.nome = 'Cerveja Amstel Lata 269ml'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));

UPDATE produtos SET estoque = 200
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.002.002/0001-02'
  AND produtos.nome = 'Cerveja Heineken Long Neck 330ml'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));

UPDATE produtos SET estoque = 150
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.002.002/0001-02'
  AND produtos.nome = 'Cerveja Corona Long Neck'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));

UPDATE produtos SET estoque = 100
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.002.002/0001-02'
  AND produtos.nome = 'Cerveja Cerpa Export Long Neck 350ml'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));

UPDATE produtos SET estoque = 80
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.002.002/0001-02'
  AND produtos.nome = 'Cerveja Paulaner Munchen Weissbier 500ml'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));

UPDATE produtos SET estoque = 300
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.003.003/0001-03'
  AND produtos.nome = 'Whisky Glenfiddich Single Malt 12 Anos 750ml Escocês'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));

UPDATE produtos SET estoque = 200
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.003.003/0001-03'
  AND produtos.nome = 'Whisky Suntory Hibiki Japanese Harmony 700ml | Blended Premium Japonês'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));

UPDATE produtos SET estoque = 150
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.003.003/0001-03'
  AND produtos.nome = 'Whisky Escocês Royal Salute 21 Anos The Signature Blend 700ml'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));

UPDATE produtos SET estoque = 100
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.003.003/0001-03'
  AND produtos.nome = 'Whisky The Balvenie PortWood 21 Anos 700ml Single Malt Escocês'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));

UPDATE produtos SET estoque = 80
FROM empresas e WHERE produtos.empresa_id = e.id AND e.cnpj = '30.003.003/0001-03'
  AND produtos.nome = 'Conhaque Martell L''Or de Jean Martell 700ml'
  AND (produtos.estoque IS NULL OR (produtos.estoque = 0 AND NOT EXISTS (
    SELECT 1 FROM itens_solicitacao_compra i WHERE i.produto_id = produtos.id)));
