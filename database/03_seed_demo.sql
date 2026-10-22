-- =====================================================================
-- Stock4event - Seed de demonstracao
-- Replica em SQL o que DemoSeedRunner + FormaPagamentoSeedRunner (Java)
-- fazem na inicializacao do backend. Idempotente (pode rodar mais de uma vez).
--   login: demo@stock4event.com   senha: demo1234  (hash BCrypt $2a, custo 10)
-- =====================================================================
INSERT INTO empresas (nome, cnpj, telefone, tipo, criado_em)
SELECT 'Mercado Demo', '12.345.678/0001-95', '(81) 99999-0000', 'AMBOS', NOW()
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE cnpj = '12.345.678/0001-95');

INSERT INTO usuarios (nome, email, senha_hash, perfil_id, empresa_id, ativo, perfil_uso, criado_em)
SELECT 'Conta Demo', 'demo@stock4event.com', '$2a$10$VpczPn3ZagP5MlW3T9SdA.vp2E0nCZRRwJTP55aSYm7URiTUkRhi2',
       (SELECT id FROM perfis WHERE LOWER(nome) = 'admin'),
       (SELECT id FROM empresas WHERE cnpj = '12.345.678/0001-95'),
       1, 'ClienteFornecedor', NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE LOWER(email) = 'demo@stock4event.com');

INSERT INTO enderecos_entrega (empresa_id, apelido, logradouro, numero, complemento, bairro, cidade, uf, cep, principal)
SELECT e.id, 'Loja', 'Av. Agamenon Magalhães', '100', 'Loja 1', 'Maurício de Nassau', 'Caruaru', 'PE', '55012-290', TRUE
FROM empresas e WHERE e.cnpj = '12.345.678/0001-95'
  AND NOT EXISTS (SELECT 1 FROM enderecos_entrega x WHERE x.empresa_id = e.id);

INSERT INTO cartoes_pagamento_salvos (empresa_id, tipo, apelido, bandeira, ultimos_digitos, numero_mascarado, validade, titular, criado_em)
SELECT e.id, 'credito', 'Cartão da loja', 'Visa', '4242', '•••• •••• •••• 4242', '12/30', 'CONTA DEMO', NOW()
FROM empresas e WHERE e.cnpj = '12.345.678/0001-95'
  AND NOT EXISTS (SELECT 1 FROM cartoes_pagamento_salvos x WHERE x.empresa_id = e.id);

INSERT INTO chaves_pix_salvas (empresa_id, apelido, tipo_chave, chave_mascarada, ultimos_digitos, criado_em)
SELECT e.id, 'CNPJ da empresa', 'cnpj', '••.•••.•••/••••-95', '95', NOW()
FROM empresas e WHERE e.cnpj = '12.345.678/0001-95'
  AND NOT EXISTS (SELECT 1 FROM chaves_pix_salvas x WHERE x.empresa_id = e.id);

INSERT INTO formas_pagamento_salvas (empresa_id, tipo, apelido, principal)
SELECT e.id, f.tipo, f.apelido, f.principal
FROM empresas e
CROSS JOIN (VALUES ('pix','Meu PIX',TRUE), ('credito','Cartão de crédito',FALSE),
                   ('debito','Cartão de débito',FALSE), ('dinheiro','Dinheiro',FALSE)) AS f(tipo, apelido, principal)
WHERE e.cnpj = '12.345.678/0001-95'
  AND NOT EXISTS (SELECT 1 FROM formas_pagamento_salvas x WHERE x.empresa_id = e.id);
