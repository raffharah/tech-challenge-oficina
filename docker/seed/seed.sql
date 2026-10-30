-- Seed do catalogo da oficina: pecas, insumos e servicos.
--
-- Executado pelo servico `seed` do docker-compose depois das migrations.
-- Idempotente: SKU (item) e nome (servico) sao unicos sem diferenciar maiusculas;
-- registros ja existentes nao sao alterados, preservando saldos movimentados pelo uso.
--
-- O valor padrao do servico e a mao de obra. Pecas e insumos nao pertencem ao
-- servico no modelo: sao incluidos na OS a parte. Cada bloco abaixo indica o kit
-- sugerido (item x quantidade) para o atendente incluir junto com o servico.

begin;

-- Troca de oleo do motor e filtro
-- Kit: INS-OLEO-5W30-1L x4, PEC-FILTRO-OLEO-3/4UNF x1, PEC-ARRUELA-BUJAO-M14 x1
insert into item_estoque (sku, nome, tipo, preco_venda, saldo_fisico) values
    ('INS-OLEO-5W30-1L', 'Óleo de motor 5W-30 sintético API SP / ILSAC GF-6A - frasco 1 L', 'INSUMO', 54.90, 60),
    ('PEC-FILTRO-OLEO-3/4UNF', 'Filtro de óleo blindado rosca 3/4"-16 UNF com válvula antidreno', 'PECA', 39.90, 25),
    ('PEC-ARRUELA-BUJAO-M14', 'Arruela de vedação do bujão do cárter em alumínio M14', 'PECA', 4.50, 80)
on conflict ((lower(sku))) do nothing;

-- Substituição de pastilhas e fluido de freio dianteiro
-- Kit: PEC-PASTILHA-FREIO-DIANT x1, INS-FLUIDO-FREIO-DOT4-500ML x1, INS-LIMPA-FREIO-300ML x1
-- Se a espessura do disco estiver abaixo do mínimo: PEC-DISCO-FREIO-DIANT x2
insert into item_estoque (sku, nome, tipo, preco_venda, saldo_fisico) values
    ('PEC-PASTILHA-FREIO-DIANT', 'Jogo de pastilhas de freio dianteiras cerâmicas com sensor de desgaste (4 unidades)', 'PECA', 189.90, 12),
    ('PEC-DISCO-FREIO-DIANT', 'Disco de freio dianteiro ventilado 256 mm (unidade)', 'PECA', 229.90, 8),
    ('INS-FLUIDO-FREIO-DOT4-500ML', 'Fluido de freio DOT 4 ponto de ebulição seco 260 °C - frasco 500 ml', 'INSUMO', 32.90, 30),
    ('INS-LIMPA-FREIO-300ML', 'Limpa-freios desengraxante spray - lata 300 ml', 'INSUMO', 24.90, 24)
on conflict ((lower(sku))) do nothing;

-- Substituição da correia dentada e bomba d'água
-- Kit: PEC-KIT-CORREIA-DENTADA x1, PEC-BOMBA-DAGUA x1, INS-ADITIVO-RADIADOR-1L x2
insert into item_estoque (sku, nome, tipo, preco_venda, saldo_fisico) values
    ('PEC-KIT-CORREIA-DENTADA', 'Kit correia dentada 137 dentes com tensionador automático', 'PECA', 349.90, 6),
    ('PEC-BOMBA-DAGUA', 'Bomba d''água com junta e rotor metálico', 'PECA', 219.90, 5),
    ('INS-ADITIVO-RADIADOR-1L', 'Aditivo concentrado para radiador orgânico (OAT) rosa - frasco 1 L', 'INSUMO', 34.90, 40)
on conflict ((lower(sku))) do nothing;

-- Revisão do sistema de ignição e admissão
-- Kit: PEC-VELA-IGNICAO-IRIDIO x4, PEC-CABO-VELA-JOGO x1, PEC-FILTRO-AR-MOTOR x1, INS-LIMPA-TBI-300ML x1
insert into item_estoque (sku, nome, tipo, preco_venda, saldo_fisico) values
    ('PEC-VELA-IGNICAO-IRIDIO', 'Vela de ignição com eletrodo de irídio rosca 14 mm folga 0,8 mm', 'PECA', 64.90, 40),
    ('PEC-CABO-VELA-JOGO', 'Jogo de cabos de vela supressivos em silicone 7 mm (4 cabos)', 'PECA', 159.90, 8),
    ('PEC-FILTRO-AR-MOTOR', 'Filtro de ar do motor em papel plissado tipo painel', 'PECA', 59.90, 15),
    ('INS-LIMPA-TBI-300ML', 'Limpador de corpo de borboleta (TBI) spray - lata 300 ml', 'INSUMO', 29.90, 20)
on conflict ((lower(sku))) do nothing;

-- Insumos de uso geral na oficina
insert into item_estoque (sku, nome, tipo, preco_venda, saldo_fisico) values
    ('INS-GRAXA-LITIO-500G', 'Graxa multiuso à base de lítio NLGI 2 - pote 500 g', 'INSUMO', 27.90, 15),
    ('INS-DESENGRIPANTE-300ML', 'Desengripante e lubrificante spray - lata 300 ml', 'INSUMO', 22.90, 20)
on conflict ((lower(sku))) do nothing;

insert into servico (nome, valor_padrao) values
    ('Troca de óleo do motor e filtro', 90.00),
    ('Substituição de pastilhas e fluido de freio dianteiro', 180.00),
    ('Substituição da correia dentada e bomba d''água', 380.00),
    ('Revisão do sistema de ignição e admissão', 150.00)
on conflict ((lower(nome))) do nothing;

commit;
