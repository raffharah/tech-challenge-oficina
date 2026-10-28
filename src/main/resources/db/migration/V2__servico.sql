create table servico (
    id bigserial primary key,
    nome varchar(200) not null,
    valor_padrao numeric(12, 2) not null check (valor_padrao > 0)
);

create unique index uk_servico_nome on servico (lower(nome));
