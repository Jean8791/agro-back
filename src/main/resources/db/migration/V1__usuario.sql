-- Ids são UUID v7 gerados pela aplicação (ver shared/BaseEntity).
create table usuario (
    id                uuid primary key,
    nome              varchar(150) not null,
    email             varchar(150) not null unique,
    senha             varchar(255) not null,
    ativo             boolean not null default true,
    -- true = usuário do produto (nós), enxerga todos os produtores
    admin             boolean not null default false,
    criado_em         timestamp not null default now(),
    atualizado_em     timestamp not null default now(),
    criado_por_id     uuid references usuario(id),
    atualizado_por_id uuid references usuario(id)
);
