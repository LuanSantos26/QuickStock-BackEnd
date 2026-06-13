INSERT INTO perfis (nome, descricao)
SELECT 'Admin', 'Administrador da empresa'
WHERE NOT EXISTS (SELECT 1 FROM perfis WHERE LOWER(nome) = 'admin');

-- ── Plataforma ───────────────────────────────────────────────────────────────
INSERT INTO empresas (nome, cnpj, telefone, tipo, descricao, logo_url, capa_url)
SELECT 'QuickStock Distribuidora', '11.111.111/0001-11', '11999990000', 'PLATAFORMA',
       'Distribuidora oficial da plataforma QuickStock',
       'https://images.unsplash.com/photo-1569336410645-b0f0eb36f47b?w=200&h=200&fit=crop',
       'https://images.unsplash.com/photo-1513558161293-cb88a25648bb?w=800&h=400&fit=crop'
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '11.111.111/0001-11');

UPDATE empresas SET
  tipo = 'PLATAFORMA',
  descricao = 'Distribuidora oficial da plataforma QuickStock',
  logo_url = 'https://images.unsplash.com/photo-1569336410645-b0f0eb36f47b?w=200&h=200&fit=crop',
  capa_url = 'https://images.unsplash.com/photo-1513558161293-cb88a25648bb?w=800&h=400&fit=crop'
WHERE cnpj = '11.111.111/0001-11';

-- ── 10 Distribuidoras fictícias ──────────────────────────────────────────────
INSERT INTO empresas (nome, cnpj, telefone, tipo, descricao, logo_url, capa_url)
SELECT 'Tonio Distribuidora', '22.222.222/0001-22', '81999887766', 'DISTRIBUIDOR',
       'Cervejas artesanais e tradicionais para revenda em Caruaru',
       'https://images.unsplash.com/photo-1618885472179-5e4740f0882c?w=200&h=200&fit=crop',
       'https://images.unsplash.com/photo-1436076863939-06870fe779c2?w=800&h=400&fit=crop'
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '22.222.222/0001-22');

INSERT INTO empresas (nome, cnpj, telefone, tipo, descricao, logo_url, capa_url)
SELECT 'Nordeste Bebidas Atacado', '33.333.333/0001-33', '81988776655', 'DISTRIBUIDOR',
       'Atacado de cervejas, refrigerantes e energéticos para o Nordeste',
       'https://images.unsplash.com/photo-1571613314887-6f763d367ccb?w=200&h=200&fit=crop',
       'https://images.unsplash.com/photo-1608270586620-248524c67de9?w=800&h=400&fit=crop'
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '33.333.333/0001-33');

INSERT INTO empresas (nome, cnpj, telefone, tipo, descricao, logo_url, capa_url)
SELECT 'Imperial Cervejas PE', '44.444.444/0001-44', '81977665544', 'DISTRIBUIDOR',
       'Distribuidora premium de cervejas importadas e nacionais',
       'https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=200&h=200&fit=crop',
       'https://images.unsplash.com/photo-1559526323-cb2f2fe2591b?w=800&h=400&fit=crop'
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '44.444.444/0001-44');

INSERT INTO empresas (nome, cnpj, telefone, tipo, descricao, logo_url, capa_url)
SELECT 'Gelada Express Caruaru', '55.555.555/0001-55', '81966554433', 'DISTRIBUIDOR',
       'Entrega rápida de bebidas geladas para bares e quiosques',
       'https://images.unsplash.com/photo-1603847709497-37f1370d1c68?w=200&h=200&fit=crop',
       'https://images.unsplash.com/photo-1569529465841-df988b0663b1?w=800&h=400&fit=crop'
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '55.555.555/0001-55');

INSERT INTO empresas (nome, cnpj, telefone, tipo, descricao, logo_url, capa_url)
SELECT 'Refri & Cia Distribuidora', '66.666.666/0001-66', '81955443322', 'DISTRIBUIDOR',
       'Refrigerantes, sucos e águas para revenda em grande volume',
       'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=200&h=200&fit=crop',
       'https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=800&h=400&fit=crop'
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '66.666.666/0001-66');

INSERT INTO empresas (nome, cnpj, telefone, tipo, descricao, logo_url, capa_url)
SELECT 'Festa Drinks Atacado', '77.777.777/0001-77', '81944332211', 'DISTRIBUIDOR',
       'Bebidas para festas juninas, eventos e feiras de Caruaru',
       'https://images.unsplash.com/photo-1514362545857-3bc16c4c7d88?w=200&h=200&fit=crop',
       'https://images.unsplash.com/photo-1470337458703-46ad1756a187?w=800&h=400&fit=crop'
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '77.777.777/0001-77');

INSERT INTO empresas (nome, cnpj, telefone, tipo, descricao, logo_url, capa_url)
SELECT 'Porto Breja Distribuidora', '88.888.888/0001-88', '81933221100', 'DISTRIBUIDOR',
       'Especialista em cervejas craft e chopes para bares',
       'https://images.unsplash.com/photo-1608270586620-248524c67de9?w=200&h=200&fit=crop',
       'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=800&h=400&fit=crop'
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '88.888.888/0001-88');

INSERT INTO empresas (nome, cnpj, telefone, tipo, descricao, logo_url, capa_url)
SELECT 'Agua & Gas Nordeste', '99.999.999/0001-99', '81922110099', 'DISTRIBUIDOR',
       'Águas minerais, gás de cozinha e bebidas não alcoólicas',
       'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=200&h=200&fit=crop',
       'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=800&h=400&fit=crop'
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '99.999.999/0001-99');

INSERT INTO empresas (nome, cnpj, telefone, tipo, descricao, logo_url, capa_url)
SELECT 'Bira Premium Atacado', '10.101.010/0001-10', '81911009988', 'DISTRIBUIDOR',
       'Cervejas premium, long necks e packs para atacado',
       'https://images.unsplash.com/photo-1618885472179-5e4740f0882c?w=200&h=200&fit=crop',
       'https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=800&h=400&fit=crop'
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '10.101.010/0001-10');

INSERT INTO empresas (nome, cnpj, telefone, tipo, descricao, logo_url, capa_url)
SELECT 'São João Bebidas', '20.202.020/0001-20', '81900998877', 'DISTRIBUIDOR',
       'Fornecedor tradicional de bebidas para festas de São João',
       'https://images.unsplash.com/photo-1569336410645-b0f0eb36f47b?w=200&h=200&fit=crop',
       'https://images.unsplash.com/photo-1514362545857-3bc16c4c7d88?w=800&h=400&fit=crop'
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '20.202.020/0001-20');

-- Atualiza logos/capas mesmo quando empresas já existem (URLs estáveis)
UPDATE empresas SET logo_url = 'https://ui-avatars.com/api/?name=QS&background=F8B125&color=fff&bold=true&size=200&format=png', capa_url = 'https://picsum.photos/seed/quickstock-capa/800/400' WHERE cnpj = '11.111.111/0001-11';
UPDATE empresas SET logo_url = 'https://ui-avatars.com/api/?name=TD&background=F8B125&color=fff&bold=true&size=200&format=png', capa_url = 'https://picsum.photos/seed/tonio-capa/800/400' WHERE cnpj = '22.222.222/0001-22';
UPDATE empresas SET logo_url = 'https://ui-avatars.com/api/?name=NB&background=F8B125&color=fff&bold=true&size=200&format=png', capa_url = 'https://picsum.photos/seed/nordeste-capa/800/400' WHERE cnpj = '33.333.333/0001-33';
UPDATE empresas SET logo_url = 'https://ui-avatars.com/api/?name=IP&background=F8B125&color=fff&bold=true&size=200&format=png', capa_url = 'https://picsum.photos/seed/imperial-capa/800/400' WHERE cnpj = '44.444.444/0001-44';
UPDATE empresas SET logo_url = 'https://ui-avatars.com/api/?name=GE&background=F8B125&color=fff&bold=true&size=200&format=png', capa_url = 'https://picsum.photos/seed/gelada-capa/800/400' WHERE cnpj = '55.555.555/0001-55';
UPDATE empresas SET logo_url = 'https://ui-avatars.com/api/?name=RC&background=F8B125&color=fff&bold=true&size=200&format=png', capa_url = 'https://picsum.photos/seed/refri-capa/800/400' WHERE cnpj = '66.666.666/0001-66';
UPDATE empresas SET logo_url = 'https://ui-avatars.com/api/?name=FD&background=F8B125&color=fff&bold=true&size=200&format=png', capa_url = 'https://picsum.photos/seed/festa-capa/800/400' WHERE cnpj = '77.777.777/0001-77';
UPDATE empresas SET logo_url = 'https://ui-avatars.com/api/?name=PB&background=F8B125&color=fff&bold=true&size=200&format=png', capa_url = 'https://picsum.photos/seed/porto-capa/800/400' WHERE cnpj = '88.888.888/0001-88';
UPDATE empresas SET logo_url = 'https://ui-avatars.com/api/?name=AG&background=F8B125&color=fff&bold=true&size=200&format=png', capa_url = 'https://picsum.photos/seed/agua-capa/800/400' WHERE cnpj = '99.999.999/0001-99';
UPDATE empresas SET logo_url = 'https://ui-avatars.com/api/?name=BP&background=F8B125&color=fff&bold=true&size=200&format=png', capa_url = 'https://picsum.photos/seed/bira-capa/800/400' WHERE cnpj = '10.101.010/0001-10';
UPDATE empresas SET logo_url = 'https://ui-avatars.com/api/?name=SJ&background=F8B125&color=fff&bold=true&size=200&format=png', capa_url = 'https://picsum.photos/seed/saojoao-capa/800/400' WHERE cnpj = '20.202.020/0001-20';

-- ── Produtos — imagens de bebidas (Unsplash) ─────────────────────────────────
-- QuickStock PLATAFORMA
INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Skol Lata 350ml', 3.50, 'UN', 'Cerveja Pilsen lata 350ml',
       'https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '11.111.111/0001-11'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Skol Lata 350ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Brahma 600ml', 5.90, 'UN', 'Cerveja long neck 600ml',
       'https://images.unsplash.com/photo-1618885472179-5e4740f0882c?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '11.111.111/0001-11'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Brahma 600ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Heineken Long Neck', 7.50, 'UN', 'Cerveja premium long neck 330ml',
       'https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '11.111.111/0001-11'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Heineken Long Neck');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Coca-Cola 2L', 9.90, 'UN', 'Refrigerante cola 2 litros',
       'https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '11.111.111/0001-11'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Coca-Cola 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Guaraná Antarctica 2L', 8.50, 'UN', 'Refrigerante guaraná 2 litros',
       'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '11.111.111/0001-11'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Guaraná Antarctica 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Água Crystal 5L', 6.50, 'UN', 'Água mineral sem gás 5 litros',
       'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '11.111.111/0001-11'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Água Crystal 5L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Red Bull 250ml', 12.90, 'UN', 'Energético lata 250ml',
       'https://images.unsplash.com/photo-1559526323-cb2f2fe2591b?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '11.111.111/0001-11'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Red Bull 250ml');

-- Tonio Distribuidora
INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Skol Lata 350ml', 3.40, 'UN', 'Cerveja Pilsen lata 350ml',
       'https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '22.222.222/0001-22'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Skol Lata 350ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Brahma 600ml', 5.80, 'UN', 'Cerveja long neck 600ml',
       'https://images.unsplash.com/photo-1618885472179-5e4740f0882c?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '22.222.222/0001-22'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Brahma 600ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Antarctica Original 600ml', 6.20, 'UN', 'Cerveja original long neck',
       'https://images.unsplash.com/photo-1569529465841-df988b0663b1?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '22.222.222/0001-22'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Antarctica Original 600ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Heineken Long Neck', 7.90, 'UN', 'Cerveja premium 330ml',
       'https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '22.222.222/0001-22'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Heineken Long Neck');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Stella Artois 330ml', 8.50, 'UN', 'Cerveja premium long neck',
       'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '22.222.222/0001-22'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Stella Artois 330ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Coca-Cola 2L', 9.50, 'UN', 'Refrigerante cola 2 litros',
       'https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '22.222.222/0001-22'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Coca-Cola 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Água Crystal 5L', 6.00, 'UN', 'Água mineral 5 litros',
       'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '22.222.222/0001-22'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Água Crystal 5L');

-- Nordeste Bebidas Atacado
INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Skol Lata 350ml', 3.30, 'CX', 'Caixa com 12 latas 350ml',
       'https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '33.333.333/0001-33'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Skol Lata 350ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Brahma 600ml', 5.70, 'UN', 'Cerveja long neck 600ml',
       'https://images.unsplash.com/photo-1618885472179-5e4740f0882c?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '33.333.333/0001-33'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Brahma 600ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Coca-Cola 2L', 9.80, 'UN', 'Refrigerante cola 2 litros',
       'https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '33.333.333/0001-33'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Coca-Cola 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Guaraná Antarctica 2L', 8.30, 'UN', 'Refrigerante guaraná 2 litros',
       'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '33.333.333/0001-33'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Guaraná Antarctica 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Red Bull 250ml', 12.50, 'UN', 'Energético lata 250ml',
       'https://images.unsplash.com/photo-1559526323-cb2f2fe2591b?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '33.333.333/0001-33'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Red Bull 250ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Heineken Long Neck', 7.70, 'UN', 'Cerveja premium 330ml',
       'https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '33.333.333/0001-33'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Heineken Long Neck');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Água Crystal 5L', 6.20, 'UN', 'Água mineral 5 litros',
       'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '33.333.333/0001-33'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Água Crystal 5L');

-- Imperial Cervejas PE
INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Heineken Long Neck', 7.90, 'UN', 'Cerveja premium importada',
       'https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '44.444.444/0001-44'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Heineken Long Neck');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Stella Artois 330ml', 8.90, 'UN', 'Cerveja premium belga',
       'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '44.444.444/0001-44'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Stella Artois 330ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Corona Extra 330ml', 9.50, 'UN', 'Cerveja mexicana long neck',
       'https://images.unsplash.com/photo-1603847709497-37f1370d1c68?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '44.444.444/0001-44'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Corona Extra 330ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Brahma 600ml', 5.90, 'UN', 'Cerveja long neck 600ml',
       'https://images.unsplash.com/photo-1618885472179-5e4740f0882c?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '44.444.444/0001-44'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Brahma 600ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Skol Lata 350ml', 3.50, 'UN', 'Cerveja Pilsen lata 350ml',
       'https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '44.444.444/0001-44'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Skol Lata 350ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Antarctica Original 600ml', 6.50, 'UN', 'Cerveja original long neck',
       'https://images.unsplash.com/photo-1569529465841-df988b0663b1?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '44.444.444/0001-44'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Antarctica Original 600ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Red Bull 250ml', 13.00, 'UN', 'Energético lata 250ml',
       'https://images.unsplash.com/photo-1559526323-cb2f2fe2591b?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '44.444.444/0001-44'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Red Bull 250ml');

-- Gelada Express Caruaru
INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Skol Lata 350ml', 3.60, 'UN', 'Cerveja gelada lata 350ml',
       'https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '55.555.555/0001-55'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Skol Lata 350ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Brahma 600ml', 6.00, 'UN', 'Cerveja gelada long neck',
       'https://images.unsplash.com/photo-1618885472179-5e4740f0882c?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '55.555.555/0001-55'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Brahma 600ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Heineken Long Neck', 8.00, 'UN', 'Cerveja premium gelada',
       'https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '55.555.555/0001-55'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Heineken Long Neck');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Coca-Cola 2L', 10.00, 'UN', 'Refrigerante gelado 2 litros',
       'https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '55.555.555/0001-55'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Coca-Cola 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Guaraná Antarctica 2L', 8.80, 'UN', 'Refrigerante guaraná gelado',
       'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '55.555.555/0001-55'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Guaraná Antarctica 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Água Crystal 5L', 6.80, 'UN', 'Água mineral gelada 5 litros',
       'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '55.555.555/0001-55'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Água Crystal 5L');

-- Refri & Cia Distribuidora
INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Coca-Cola 2L', 9.50, 'UN', 'Refrigerante cola 2 litros',
       'https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '66.666.666/0001-66'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Coca-Cola 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Guaraná Antarctica 2L', 8.20, 'UN', 'Refrigerante guaraná 2 litros',
       'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '66.666.666/0001-66'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Guaraná Antarctica 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Fanta Laranja 2L', 8.00, 'UN', 'Refrigerante laranja 2 litros',
       'https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '66.666.666/0001-66'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Fanta Laranja 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Água Crystal 5L', 6.00, 'UN', 'Água mineral 5 litros',
       'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '66.666.666/0001-66'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Água Crystal 5L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Suco Del Valle 1L', 7.50, 'UN', 'Suco de laranja 1 litro',
       'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '66.666.666/0001-66'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Suco Del Valle 1L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Red Bull 250ml', 12.00, 'UN', 'Energético lata 250ml',
       'https://images.unsplash.com/photo-1559526323-cb2f2fe2591b?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '66.666.666/0001-66'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Red Bull 250ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Skol Lata 350ml', 3.40, 'UN', 'Cerveja Pilsen lata 350ml',
       'https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '66.666.666/0001-66'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Skol Lata 350ml');

-- Festa Drinks Atacado
INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Skol Lata 350ml', 3.20, 'CX', 'Caixa festa 12 latas',
       'https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '77.777.777/0001-77'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Skol Lata 350ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Brahma 600ml', 5.50, 'UN', 'Cerveja long neck festa',
       'https://images.unsplash.com/photo-1618885472179-5e4740f0882c?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '77.777.777/0001-77'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Brahma 600ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Coca-Cola 2L', 9.00, 'UN', 'Refrigerante para festa',
       'https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '77.777.777/0001-77'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Coca-Cola 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Guaraná Antarctica 2L', 7.90, 'UN', 'Refrigerante guaraná festa',
       'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '77.777.777/0001-77'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Guaraná Antarctica 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Água Crystal 5L', 5.80, 'UN', 'Água para eventos',
       'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '77.777.777/0001-77'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Água Crystal 5L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Heineken Long Neck', 7.50, 'UN', 'Cerveja premium festa',
       'https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '77.777.777/0001-77'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Heineken Long Neck');

-- Porto Breja Distribuidora
INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Heineken Long Neck', 8.20, 'UN', 'Cerveja craft premium',
       'https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '88.888.888/0001-88'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Heineken Long Neck');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Stella Artois 330ml', 9.20, 'UN', 'Cerveja premium belga',
       'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '88.888.888/0001-88'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Stella Artois 330ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Corona Extra 330ml', 9.80, 'UN', 'Cerveja mexicana',
       'https://images.unsplash.com/photo-1603847709497-37f1370d1c68?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '88.888.888/0001-88'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Corona Extra 330ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Antarctica Original 600ml', 6.80, 'UN', 'Cerveja original',
       'https://images.unsplash.com/photo-1569529465841-df988b0663b1?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '88.888.888/0001-88'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Antarctica Original 600ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Brahma 600ml', 6.10, 'UN', 'Cerveja long neck',
       'https://images.unsplash.com/photo-1618885472179-5e4740f0882c?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '88.888.888/0001-88'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Brahma 600ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Skol Lata 350ml', 3.60, 'UN', 'Cerveja Pilsen lata',
       'https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '88.888.888/0001-88'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Skol Lata 350ml');

-- Agua & Gas Nordeste
INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Água Crystal 5L', 5.90, 'UN', 'Água mineral 5 litros',
       'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '99.999.999/0001-99'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Água Crystal 5L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Água Crystal 1,5L', 2.50, 'UN', 'Água mineral 1,5 litros',
       'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '99.999.999/0001-99'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Água Crystal 1,5L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Coca-Cola 2L', 9.20, 'UN', 'Refrigerante cola 2 litros',
       'https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '99.999.999/0001-99'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Coca-Cola 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Guaraná Antarctica 2L', 8.00, 'UN', 'Refrigerante guaraná',
       'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '99.999.999/0001-99'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Guaraná Antarctica 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Suco Del Valle 1L', 7.00, 'UN', 'Suco de laranja',
       'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '99.999.999/0001-99'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Suco Del Valle 1L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Skol Lata 350ml', 3.30, 'UN', 'Cerveja Pilsen lata',
       'https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '99.999.999/0001-99'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Skol Lata 350ml');

-- Bira Premium Atacado
INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Heineken Long Neck', 7.80, 'UN', 'Cerveja premium long neck',
       'https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '10.101.010/0001-10'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Heineken Long Neck');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Stella Artois 330ml', 8.70, 'UN', 'Cerveja premium belga',
       'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '10.101.010/0001-10'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Stella Artois 330ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Brahma 600ml', 5.80, 'UN', 'Cerveja long neck 600ml',
       'https://images.unsplash.com/photo-1618885472179-5e4740f0882c?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '10.101.010/0001-10'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Brahma 600ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Skol Lata 350ml', 3.40, 'UN', 'Cerveja Pilsen lata',
       'https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '10.101.010/0001-10'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Skol Lata 350ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Corona Extra 330ml', 9.30, 'UN', 'Cerveja mexicana',
       'https://images.unsplash.com/photo-1603847709497-37f1370d1c68?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '10.101.010/0001-10'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Corona Extra 330ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Red Bull 250ml', 12.50, 'UN', 'Energético lata',
       'https://images.unsplash.com/photo-1559526323-cb2f2fe2591b?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '10.101.010/0001-10'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Red Bull 250ml');

-- São João Bebidas
INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Skol Lata 350ml', 3.10, 'CX', 'Caixa São João 12 latas',
       'https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '20.202.020/0001-20'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Skol Lata 350ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Brahma 600ml', 5.40, 'UN', 'Cerveja festa junina',
       'https://images.unsplash.com/photo-1618885472179-5e4740f0882c?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '20.202.020/0001-20'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Brahma 600ml');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Coca-Cola 2L', 8.90, 'UN', 'Refrigerante festa',
       'https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '20.202.020/0001-20'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Coca-Cola 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Guaraná Antarctica 2L', 7.80, 'UN', 'Refrigerante guaraná',
       'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '20.202.020/0001-20'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Guaraná Antarctica 2L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Água Crystal 5L', 5.50, 'UN', 'Água para festa',
       'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '20.202.020/0001-20'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Água Crystal 5L');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Heineken Long Neck', 7.20, 'UN', 'Cerveja premium festa',
       'https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '20.202.020/0001-20'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Heineken Long Neck');

INSERT INTO produtos (empresa_id, nome, preco_venda, unidade, descricao, imagem_url, ativo)
SELECT e.id, 'Red Bull 250ml', 11.90, 'UN', 'Energético festa',
       'https://images.unsplash.com/photo-1559526323-cb2f2fe2591b?w=400&h=400&fit=crop', 1
FROM empresas e WHERE e.cnpj = '20.202.020/0001-20'
AND NOT EXISTS (SELECT 1 FROM produtos p WHERE p.empresa_id = e.id AND p.nome = 'Red Bull 250ml');

-- Textos, acentos e imagens sao corrigidos pelo MarketplaceDataFixRunner (Java/UTF-8)
