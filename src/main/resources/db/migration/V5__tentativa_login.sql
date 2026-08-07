-- Proteção contra força bruta: acontece antes de saber quem é o usuário,
-- então é global, não pertence a produtor nenhum.
create table tentativa_login (
    id                uuid primary key,
    tipo              varchar(20) not null,
    chave             varchar(255) not null,
    criado_em         timestamp not null default now(),
    atualizado_em     timestamp not null default now(),
    criado_por_id     uuid references usuario(id),
    atualizado_por_id uuid references usuario(id)
);

create index idx_tentativa_login_busca on tentativa_login(tipo, chave, criado_em);
