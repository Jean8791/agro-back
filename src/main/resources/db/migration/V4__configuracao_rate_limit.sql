-- Configuração global do produto (não pertence a produtor nenhum).
-- Linha única: o check garante que não exista mais de uma.
create table configuracao_rate_limit (
    id                   uuid primary key,
    linha_unica          boolean not null default true check (linha_unica),
    max_tentativas_email integer not null,
    max_tentativas_ip    integer not null,
    janela_minutos       integer not null,
    bloqueio_minutos     integer not null,
    criado_em            timestamp not null default now(),
    atualizado_em        timestamp not null default now(),
    criado_por_id        uuid references usuario(id),
    atualizado_por_id    uuid references usuario(id),
    constraint uk_configuracao_rate_limit_linha_unica unique (linha_unica)
);

insert into configuracao_rate_limit
    (id, max_tentativas_email, max_tentativas_ip, janela_minutos, bloqueio_minutos)
values
    ('01890000-0000-7000-8000-000000000001', 5, 20, 15, 15);
