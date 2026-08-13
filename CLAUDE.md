# agro-back

Backend Java/Quarkus (Postgres) para ferramenta de agronegócio.
Pacote base: `br.com.codenest.agro`

## Convenção de estrutura de pacotes

### Regra geral de nomenclatura

- Pastas/pacotes que representam **domínio de negócio** (o que existe no mundo
  real do agronegócio) ficam em **português**.
- Pastas/arquivos **técnicos** (camadas de arquitetura, infraestrutura,
  conceitos do próprio framework) ficam em **inglês**.
- Regra prática: se o dono da fazenda entenderia o nome, é português. Se é
  algo que só o programador entende (Service, Repository, DTO, Exception,
  Config), é inglês.

### Package by feature, não por camada

Não usar pastas globais tipo `resource/`, `service/`, `repository/`, `dto/`
na raiz contendo tudo. Cada domínio de negócio é uma pasta própria e
autocontida, direto na raiz do pacote base (`br.com.codenest.agro/`), com
suas próprias subpastas técnicas dentro.

```
br.com.codenest.agro/
  propriedade/              -> domínio de negócio (fazendas/áreas do produtor)
    PropriedadeResource.java
    PropriedadeService.java
    PropriedadeRepository.java
    Propriedade.java          (entidade)
    dto/
      PropriedadeRequest.java
      PropriedadeResponse.java

  cultura/                  -> domínio de negócio (culturas plantadas)
    CulturaResource.java
    CulturaService.java
    CulturaRepository.java
    Cultura.java

  clima/                    -> domínio de negócio (histórico climático)
    ClimaResource.java
    ClimaService.java
    ClimaRepository.java
    DadoClimatico.java
    dto/
    integracao/
      ClimaApiClient.java      (rest client para API externa de clima)
    job/
      AtualizarClimaJob.java   (job agendado)

  rendimento/                -> domínio de negócio (cálculo de rendimento)
    RendimentoResource.java
    RendimentoService.java
    RendimentoRepository.java
    Rendimento.java
    dto/
      RendimentoRequest.java
      RendimentoResponse.java

  usuario/                   -> domínio de negócio (usuário do sistema)
    UsuarioResource.java
    entity/
      Usuario.java
    enums/
      Perfil.java
    dto/
      UsuarioRequest.java
      UsuarioResponse.java
    repository/
      UsuarioRepository.java
    service/
      UsuarioService.java

  auth/                      -> companheiro técnico do usuario (login/JWT)
    AuthResource.java
    dto/
      LoginRequest.java
      LoginResponse.java
    service/
      AuthService.java         (depende de usuario/, nunca o contrário)

  shared/                    -> técnico, transversal a mais de um domínio
    BaseEntity.java             (id, criadoEm/atualizadoEm, criadoPorId/atualizadoPorId)
    AuditContext.java           (resolve o usuário autenticado a partir do JWT)
    exception/
      GlobalExceptionMapper.java
    config/
    enums/                    (somente enums usados por 2+ domínios)
```

### Regras adicionais

1. Dentro de cada domínio, ainda separar por responsabilidade (Resource,
   Service, Repository, DTO), mas nunca espalhar um mesmo domínio em pastas
   técnicas soltas na raiz do projeto.
2. Enums e DTOs ficam dentro do próprio domínio por padrão. Só sobem para
   `shared/` quando forem genuinamente usados por mais de um domínio — nunca
   "por precaução".
3. Evitar dependência circular entre domínios (ex: `clima/` não deve
   depender de `rendimento/`; a dependência é sempre de "cima pra baixo" na
   hierarquia de negócio).
4. Não criar uma pasta wrapper genérica tipo `programas/` ou `modulos/`
   acima dos domínios — cada domínio fica direto na raiz do pacote base.
5. Convenções de nome de classe seguem o padrão Quarkus/Java:
   `NomeDominioResource`, `NomeDominioService`, `NomeDominioRepository`,
   `NomeDominio` (entidade), `NomeDominioRequest`/`NomeDominioResponse` (DTOs).
6. Um recurso técnico fortemente acoplado a um único domínio (ex: `auth`,
   que só existe pra autenticar um `usuario`) pode virar uma pasta própria
   na raiz mesmo sem ser "algo que o dono da fazenda pediria" — desde que a
   dependência seja sempre em uma direção só (ex: `auth` depende de
   `usuario`, `usuario` nunca depende de `auth`). Se a dependência virar
   bidirecional, é sinal de que deveria estar dentro do domínio principal.
7. Toda entidade JPA estende `shared/BaseEntity.java` (id, criadoEm,
   atualizadoEm, criadoPorId, atualizadoPorId — preenchidos automaticamente).
   Não recriar esses campos em cada entidade.

Aplicar essa estrutura ao criar novos domínios/telas no projeto.

## Autorização: papel e permissão

`Papel` era um enum de três valores (`ADMIN`, `OPERACIONAL`, `CONSULTA`) e
virou **tabela** (`papel/`, migration `V7`), para o ADMIN montar os papéis que
a operação dele pede sem depender de quem escreve o código.

- **Permissão é enum** (`papel/enums/Permissao.java`), no formato
  `recurso:acao` — `usuario:inserir`. Precisa ser constante de compilação
  porque o `@RolesAllowed` a cita: daí a classe aninhada `Permissao.Codigos`.
  A tabela `papel_permissao` guarda só o par papel↔permissão.
- **Recurso × Ação** (`Recurso`, `Acao`) existem para o front desenhar a
  matriz de checkbox. `GET /papeis/permissoes` devolve essa matriz montada, de
  modo que uma tela nova aparece lá sem mexer no front — basta acrescentar o
  valor em `Recurso` e as entradas em `Permissao`.
- **Papel de sistema** tem `produtor_id` nulo e `sistema = true`: vale para
  todo produtor, ninguém edita. Os ids ficam em `papel/PapeisDeSistema.java`,
  fixos porque a migration os insere literalmente. Papel criado por um ADMIN
  nasce com o `produtor_id` dele.
- **As permissões vão na claim `groups`** do access token
  (`AuthService.gerarTokens`), no lugar do papel. É ela que o `@RolesAllowed`
  lê, então autorizar por permissão não custa interceptor nem consulta ao
  banco por requisição. O preço: tirar permissão de alguém só passa a valer no
  próximo refresh (access token dura 15 min). O super admin não tem papel e
  entra em `groups` como `Autorizacao.SUPER_ADMIN`.
- **`@RolesAllowed` é por método, nunca na classe** — quem pode listar não
  necessariamente pode excluir. Todo endpoint aceita também
  `Autorizacao.SUPER_ADMIN`. Esconder o botão no front é conforto; quem barra
  de verdade é a anotação.

Ao criar um domínio novo: acrescentar o valor em `Recurso`, as permissões em
`Permissao` (+ `Codigos`), anotar cada método do Resource, e dar as novas
permissões ao papel Administrador numa migration.

`produtor/service/EscopoProdutor.java` resolve sobre qual produtor a operação
age (o do token, ou o informado quando é super admin). Todo service que
recebe `produtorId` por query deve usá-lo em vez de repetir a regra.
