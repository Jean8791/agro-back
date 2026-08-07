-- O cliente que assina: um produtor tem N propriedades (as fazendas dele).
create table produtor (
    id                uuid primary key,
    nome              varchar(150) not null,
    documento         varchar(14) not null unique,
    tipo_pessoa       varchar(2) not null,
    ativo             boolean not null default true,
    criado_em         timestamp not null default now(),
    atualizado_em     timestamp not null default now(),
    criado_por_id     uuid references usuario(id),
    atualizado_por_id uuid references usuario(id)
);

-- Vínculo N:N: a mesma pessoa pode atender mais de um produtor, com papel e
-- situação próprios em cada um (ex: agrônomo que atende três clientes).
create table produtor_usuario (
    id                uuid primary key,
    produtor_id       uuid not null references produtor(id),
    usuario_id        uuid not null references usuario(id),
    papel             varchar(30) not null,
    ativo             boolean not null default true,
    criado_em         timestamp not null default now(),
    atualizado_em     timestamp not null default now(),
    criado_por_id     uuid references usuario(id),
    atualizado_por_id uuid references usuario(id),
    constraint uk_produtor_usuario unique (produtor_id, usuario_id)
);

create index idx_produtor_usuario_usuario on produtor_usuario(usuario_id);
