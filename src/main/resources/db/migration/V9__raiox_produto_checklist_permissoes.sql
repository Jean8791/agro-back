-- O recurso único RAIOX (produtos + checklist juntos) virou dois recursos
-- separados — RAIOXPRODUTO e RAIOXCHECKLIST — para o ADMIN poder dar a
-- permissão de configurar o catálogo e a de configurar o checklist a papéis
-- diferentes, em vez de ser tudo ou nada.
--
-- Todo papel que tinha RAIOX_<acao> ganha RAIOXPRODUTO_<acao> e
-- RAIOXCHECKLIST_<acao> equivalentes (dono do papel decide depois se quer
-- separar), e a permissão antiga é removida — ela deixou de existir no
-- enum Permissao, não faz mais sentido sobreviver na tabela.
insert into papel_permissao (papel_id, permissao)
select papel_id, replace(permissao, 'RAIOX_', 'RAIOXPRODUTO_')
from papel_permissao
where permissao like 'RAIOX\_%' escape '\'
on conflict do nothing;

insert into papel_permissao (papel_id, permissao)
select papel_id, replace(permissao, 'RAIOX_', 'RAIOXCHECKLIST_')
from papel_permissao
where permissao like 'RAIOX\_%' escape '\'
on conflict do nothing;

delete from papel_permissao where permissao like 'RAIOX\_%' escape '\';
