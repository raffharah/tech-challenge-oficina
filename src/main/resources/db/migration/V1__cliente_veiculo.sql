create table cliente (
    id bigserial primary key,
    nome varchar(200) not null,
    cpf_cnpj varchar(14) not null unique
);

create table veiculo (
    id bigserial primary key,
    placa varchar(7) not null unique,
    marca varchar(80) not null,
    modelo varchar(80) not null,
    ano integer not null,
    cliente_id bigint not null references cliente (id)
);

create index idx_veiculo_cliente_id on veiculo (cliente_id);
