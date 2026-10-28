create table ordem_servico (
    id bigserial primary key,
    veiculo_id bigint not null references veiculo (id),
    cpf_cnpj varchar(14) not null,
    placa varchar(7) not null,
    status varchar(30) not null,
    total numeric(12, 2),
    aberta_em timestamptz not null
);

create index idx_ordem_servico_veiculo_id on ordem_servico (veiculo_id);

create table ordem_servico_servico (
    ordem_servico_id bigint not null references ordem_servico (id),
    servico_id bigint not null references servico (id),
    nome varchar(200) not null,
    valor numeric(12, 2) not null,
    primary key (ordem_servico_id, servico_id)
);

create index idx_ordem_servico_servico_servico_id on ordem_servico_servico (servico_id);

create table ordem_servico_item (
    ordem_servico_id bigint not null references ordem_servico (id),
    item_id bigint not null references item_estoque (id),
    sku varchar(60) not null,
    nome varchar(200) not null,
    quantidade int not null check (quantidade > 0),
    preco_unitario numeric(12, 2) not null,
    primary key (ordem_servico_id, item_id)
);

create index idx_ordem_servico_item_item_id on ordem_servico_item (item_id);

create table ordem_servico_historico (
    id bigserial primary key,
    ordem_servico_id bigint not null references ordem_servico (id),
    status varchar(30) not null,
    data_hora timestamptz not null
);

create index idx_ordem_servico_historico_ordem on ordem_servico_historico (ordem_servico_id);
