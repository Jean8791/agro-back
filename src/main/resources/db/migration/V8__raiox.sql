-- Raio-X do Preço Mínimo: catálogo de produtos/atividades, checklist de
-- custos e simulações salvas. Base e checklist são dado do produtor (cada um
-- mantém o próprio), não configuração global — por isso produtor_id not null
-- e sem o conceito de "sistema" que papel tem.

create table raiox_produto_base (
    id                     uuid primary key,
    produtor_id            uuid not null references produtor(id),
    modulo                 varchar(10) not null check (modulo in ('VEGETAL', 'ANIMAL')),
    categoria              varchar(80) not null,
    produto                varchar(120) not null,
    unidade_tecnica        varchar(40) not null,
    unidade_comercial      varchar(40) not null,
    quantidade_por_unidade numeric(14, 4) not null,
    unidade_preco          varchar(30) not null,
    tipo_producao_animal   varchar(12) check (tipo_producao_animal in ('CONTINUA', 'LOTE')),
    nota                   varchar(400),
    fonte                  varchar(300),
    criado_em              timestamp not null default now(),
    atualizado_em          timestamp not null default now(),
    criado_por_id          uuid references usuario(id),
    atualizado_por_id      uuid references usuario(id)
);

create index idx_raiox_produto_base_produtor on raiox_produto_base(produtor_id);

create table raiox_checklist_item (
    id                uuid primary key,
    produtor_id       uuid not null references produtor(id),
    modulo            varchar(10) not null check (modulo in ('VEGETAL', 'ANIMAL', 'AMBOS')),
    rotulo            varchar(160) not null,
    nota              varchar(300),
    criado_em         timestamp not null default now(),
    atualizado_em     timestamp not null default now(),
    criado_por_id     uuid references usuario(id),
    atualizado_por_id uuid references usuario(id)
);

create index idx_raiox_checklist_produtor on raiox_checklist_item(produtor_id);

-- formulario/resultado guardam exatamente o JSON que a calculadora (front)
-- produziu — o back não recalcula, só grava. modulo/produto/semaforo/
-- preco_minimo são cópias soltas dos mesmos dados, só para a listagem pintar
-- a grid sem abrir os dois JSONs.
create table raiox_simulacao (
    id                uuid primary key,
    produtor_id       uuid not null references produtor(id),
    nome              varchar(160) not null,
    modulo            varchar(10) not null check (modulo in ('VEGETAL', 'ANIMAL')),
    produto           varchar(120) not null,
    semaforo          varchar(10) not null check (semaforo in ('FILL', 'RED', 'YELLOW', 'GREEN')),
    preco_minimo      numeric(14, 2),
    formulario        jsonb not null,
    resultado         jsonb not null,
    criado_em         timestamp not null default now(),
    atualizado_em     timestamp not null default now(),
    criado_por_id     uuid references usuario(id),
    atualizado_por_id uuid references usuario(id)
);

create index idx_raiox_simulacao_produtor on raiox_simulacao(produtor_id);

-- Administrador ganha as 4 permissões de RAIOX (produtos + checklist, mesmo
-- recurso — telas irmãs editadas pela mesma pessoa) e as 3 de RAIOXSIMULACAO.
-- Operacional ("lança e edita os dados do dia a dia") ganha listar/inserir
-- simulação. Consulta ganha só listar.
insert into papel_permissao (papel_id, permissao) values
    ('01890000-0000-7000-8000-000000000010', 'RAIOX_LISTAR'),
    ('01890000-0000-7000-8000-000000000010', 'RAIOX_INSERIR'),
    ('01890000-0000-7000-8000-000000000010', 'RAIOX_EDITAR'),
    ('01890000-0000-7000-8000-000000000010', 'RAIOX_EXCLUIR'),
    ('01890000-0000-7000-8000-000000000010', 'RAIOXSIMULACAO_LISTAR'),
    ('01890000-0000-7000-8000-000000000010', 'RAIOXSIMULACAO_INSERIR'),
    ('01890000-0000-7000-8000-000000000010', 'RAIOXSIMULACAO_EXCLUIR'),
    ('01890000-0000-7000-8000-000000000011', 'RAIOXSIMULACAO_LISTAR'),
    ('01890000-0000-7000-8000-000000000011', 'RAIOXSIMULACAO_INSERIR'),
    ('01890000-0000-7000-8000-000000000012', 'RAIOXSIMULACAO_LISTAR');
