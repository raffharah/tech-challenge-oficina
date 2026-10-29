create table item_estoque (
    id bigserial primary key,
    sku varchar(60) not null,
    nome varchar(200) not null,
    tipo varchar(10) not null check (tipo in ('PECA', 'INSUMO')),
    preco_venda numeric(12, 2) not null check (preco_venda > 0),
    saldo_fisico int not null check (saldo_fisico >= 0),
    versao bigint not null default 0
);

create unique index uk_item_estoque_sku on item_estoque (lower(sku));
