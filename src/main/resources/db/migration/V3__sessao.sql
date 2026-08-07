create table sessao (
    id                 uuid primary key,
    usuario_id         uuid not null references usuario(id),
    -- produtor escolhido no login; nulo para sessão de super admin
    produtor_id        uuid references produtor(id),
    refresh_token_hash varchar(255) not null unique,
    expira_em          timestamp not null,
    revogada_em        timestamp,
    criado_em          timestamp not null default now(),
    atualizado_em      timestamp not null default now(),
    criado_por_id      uuid references usuario(id),
    atualizado_por_id  uuid references usuario(id)
);

create index idx_sessao_usuario_id on sessao(usuario_id);
