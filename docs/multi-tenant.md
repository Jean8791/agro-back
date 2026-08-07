# Multi-tenant, planos e permissões

Contexto: o agro é vendido para vários agricultores. Cada cliente é um
**produtor**, com dados isolados, e existe um usuário nosso que cadastra
esses clientes.

Base de referência: `~/workspace/codenest/marknow-api` (Quarkus, mesmo autor),
que já resolve esse problema em produção.

---

## 1. Modelo

```
produtor              id, nome, documento, tipo_pessoa, ativo          [FEITO]
produtor_usuario      produtor_id, usuario_id, papel, ativo   (N:N)    [FEITO]
usuario               + admin (boolean)  -- super admin, sem tenant    [FEITO]

plano                 id, codigo, nome, valor, trial, status           [a fazer]
plano_tela            plano_id, tela         -- módulos liberados      [a fazer]
assinatura            produtor_id, plano_id, status,
                      fim_periodo_atual, tolerancia_ate                [a fazer]

tipo_usuario          id, codigo, nome                                 [a fazer]
tipo_usuario_acao     tipo_usuario_id, tela, acao                      [a fazer]
convite               codigo, email, expira_em, max_usos, qtd_usado    [a fazer]
```

`tela` e `acao` serão enums do código, semeados por migration. O admin marca
o que cada plano/tipo tem; **nunca digita tela nova** — assim o banco não
consegue apontar para uma tela que não existe.

**Permissão efetiva** (quando os passos 3 e 4 existirem):

```
pode(tela, acao) =
      tela ∈ plano_tela(assinatura do produtor)
  AND (tela, acao) ∈ tipo_usuario_acao(papel do vínculo)
```

Plano responde "o que esse cliente contratou"; tipo de usuário responde "o
que essa pessoa faz". Colapsar os dois numa tabela só trava na primeira vez
que um cliente precisar de duas pessoas com papéis diferentes.

## 2. Isolamento — a parte que não pode falhar   [FEITO]

Filtrar por produtor à mão em cada query é garantia de esquecer em alguma, e
esquecer significa um produtor vendo a lavoura do outro. São duas camadas:

```java
@FilterDef(name = "produtorFilter",
           parameters = @ParamDef(name = "produtorId", type = UUID.class))
@Filter(name = "produtorFilter", condition = "produtor_id = :produtorId")
public abstract class BaseTenantEntity extends BaseEntity { ... }
```

1. `shared/BaseTenantEntity` declara o filtro e grava o `produtor_id` sozinho
   no `@PrePersist`.
2. `shared/tenant/TenantFilter` (ContainerRequestFilter) roda antes de
   qualquer Resource e, em ordem: pula `/auth` → resolve o usuário do token e
   valida se está ativo → **se for super admin, retorna sem ligar filtro
   nenhum** → senão exige o produtor no token e valida se está ativo →
   **habilita o filtro do Hibernate na sessão** → confere se o vínculo
   usuário↔produtor está ativo.

> Entidades **globais** (usuario, produtor, produtor_usuario,
> configuracao_rate_limit, tentativa_login) estendem `BaseEntity` e ficam de
> fora do filtro. Todo domínio de negócio novo — `propriedade`, `cultura`,
> `clima`, `rendimento` — estende **`BaseTenantEntity`**.

Testado: usuário de um produtor não vê o outro na listagem; buscar, bloquear
ou excluir registro alheio por id direto devolve **404** (e não 403, que já
confirmaria a existência do id); passar `?produtorId=` de outro produtor é
ignorado — só super admin escolhe produtor.

## 3. Autenticação   [FEITO]

- JWT com `sub` (UUID do usuário), `groups` = papel do vínculo (ou
  `SUPER_ADMIN`) e claim `produtor`.
- Login escolhe o vínculo: com um só, entra direto; com vários, exige
  `produtorId` no request — entrar num escolhido por acaso seria pior do que
  perguntar.
- **Permissão fina não vai no token**: engorda e fica obsoleta por até 15 min
  (TTL do access). Virá do `GET /auth/me`.

## 4. Autorização — o que falta

`@RolesAllowed` não sabe de tela/ação. Entra uma anotação própria:

```java
@RequerPermissao(tela = Tela.USUARIO, acao = Acao.EXCLUIR)
```

Um interceptor CDI resolve produtor + papel do token, consulta as permissões
(com cache por produtor+tipo, invalidado ao salvar plano/tipo) e responde
**403** quando não pode.

> Esconder o botão no front **não é** autorização. Sem esse interceptor, a
> tela de permissões é decorativa e um `curl` passa por cima dela.

## 5. Por onde continuar

1. **`GET /auth/me`** — usuário, produtor e permissões; o front precisa para
   montar menu e botoeira sem decodificar o token por conta própria.
2. **Paginação (`PageDTO`) em `GET /usuarios`** — o grid do front já manda
   `offset`/`limit`/`sort` e o back ignora; hoje ele roda em `modo="cliente"`.
   É o passo mais barato e destrava a pasta `pesquisa-avancada/`, vazia.
3. **`plano` / `assinatura`** + política de acesso (tolerância, trial).
4. **`tipo_usuario` + `@RequerPermissao`** — depende de 1 e 3.
5. **`convite`** para cadastrar cliente sem abrir signup público.

## 6. Fora de escopo declarado

Gateway de pagamento, limite de uso por plano (ex: máximo de propriedades),
troca de produtor sem novo login, e histórico de alteração de permissões.

## 7. Dívidas conhecidas

- `V6__seed_super_admin.sql` versiona a senha `admin123`. Trocar antes de
  qualquer ambiente que não seja local — e lembrar que o histórico do git
  guarda o valor antigo.
- `privateKey.pem` está no `.gitignore`: quem clonar precisa gerar o par de
  chaves antes de subir a aplicação.
- O access token continua sem revogação instantânea (só o refresh é
  revogável). Por isso o TTL é de 15 min. Revogar na hora exigiria consultar
  o banco a cada requisição, o que anularia o JWT stateless.
