-- Papel deixa de ser um enum de três valores fixos e vira tabela: o ADMIN
-- monta os papéis que a operação dele pede, escolhendo permissões numa matriz
-- recurso x ação. O que o código continua conhecendo é a permissão
-- (`usuario:inserir`), porque é ela que o @RolesAllowed de cada endpoint cita.
create table papel (
    id                uuid primary key,
    nome              varchar(60) not null,
    descricao         varchar(200),
    -- nulo = papel de sistema, que vale para todo produtor
    produtor_id       uuid references produtor(id),
    sistema           boolean not null default false,
    criado_em         timestamp not null default now(),
    atualizado_em     timestamp not null default now(),
    criado_por_id     uuid references usuario(id),
    atualizado_por_id uuid references usuario(id),
    constraint uk_papel_produtor_nome unique (produtor_id, nome)
);

create table papel_permissao (
    papel_id  uuid not null references papel(id) on delete cascade,
    permissao varchar(60) not null,
    primary key (papel_id, permissao)
);

-- Os três papéis que existiam como enum, agora como linha. `sistema = true`
-- os torna somente leitura: podem ser usados, nunca alterados nem excluídos.
insert into papel (id, nome, descricao, produtor_id, sistema) values
    ('01890000-0000-7000-8000-000000000010', 'Administrador',
     'Administra o produtor: cadastra usuários, define papéis e configura.', null, true),
    ('01890000-0000-7000-8000-000000000011', 'Operacional',
     'Lança e edita os dados do dia a dia.', null, true),
    ('01890000-0000-7000-8000-000000000012', 'Consulta',
     'Só consulta: não altera nada.', null, true);

insert into papel_permissao (papel_id, permissao) values
    -- Administrador: tudo o que existe hoje
    ('01890000-0000-7000-8000-000000000010', 'USUARIO_LISTAR'),
    ('01890000-0000-7000-8000-000000000010', 'USUARIO_INSERIR'),
    ('01890000-0000-7000-8000-000000000010', 'USUARIO_EDITAR'),
    ('01890000-0000-7000-8000-000000000010', 'USUARIO_EXCLUIR'),
    ('01890000-0000-7000-8000-000000000010', 'USUARIO_BLOQUEAR'),
    ('01890000-0000-7000-8000-000000000010', 'PAPEL_LISTAR'),
    ('01890000-0000-7000-8000-000000000010', 'PAPEL_INSERIR'),
    ('01890000-0000-7000-8000-000000000010', 'PAPEL_EDITAR'),
    ('01890000-0000-7000-8000-000000000010', 'PAPEL_EXCLUIR'),
    -- Operacional e Consulta enxergam a equipe, e nada além disso. Quando os
    -- domínios de negócio existirem, é aqui que eles ganham as permissões
    -- deles — ou o ADMIN monta um papel próprio e não mexe nestes.
    ('01890000-0000-7000-8000-000000000011', 'USUARIO_LISTAR'),
    ('01890000-0000-7000-8000-000000000012', 'USUARIO_LISTAR');

-- O vínculo passa a apontar para o papel. O varchar sai depois do backfill:
-- ele não tem mais como representar um papel que o ADMIN criou.
alter table produtor_usuario add column papel_id uuid references papel(id);

update produtor_usuario set papel_id = case papel
    when 'ADMIN'       then '01890000-0000-7000-8000-000000000010'::uuid
    when 'OPERACIONAL' then '01890000-0000-7000-8000-000000000011'::uuid
    when 'CONSULTA'    then '01890000-0000-7000-8000-000000000012'::uuid
end;

alter table produtor_usuario alter column papel_id set not null;
alter table produtor_usuario drop column papel;

create index idx_papel_produtor on papel(produtor_id);
create index idx_produtor_usuario_papel on produtor_usuario(papel_id);
