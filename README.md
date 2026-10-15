# agro-back

API REST multi-tenant de gestão para o agronegócio. Cada cliente é um **produtor** com dados isolados, e o módulo **Raio-X** ajuda o produtor a descobrir o preço mínimo de venda de cada produto a partir dos seus custos.

O front-end está em [agro-front](https://github.com/Jean8791/agro-front).

## Stack

- **Java 25** e **Quarkus 3**
- Quarkus REST + Jackson, Hibernate ORM com Panache e Hibernate Validator
- **PostgreSQL 16** com migrations versionadas no **Flyway**
- Autenticação **JWT** (SmallRye JWT) com access token e refresh token
- Quarkus Scheduler, YAML config
- Testes com JUnit e REST Assured
- Docker (build multiestágio, pronto para Render)

## Funcionalidades

- **Autenticação:** login, refresh token com rotação, logout e revogação de sessão quando o usuário é bloqueado.
- **Proteção contra força bruta:** rate limiting de tentativas de login por e-mail e por IP, com limites configuráveis pela API.
- **Multi-tenant:** isolamento por produtor feito com filtro do Hibernate (`@Filter`) aplicado em toda requisição, sem depender de lembrar o `WHERE` em cada query.
- **Papéis e permissões:** matriz de recurso × ação por papel, com papéis de sistema e papéis customizados por produtor.
- **Cadastros:** produtores (PF/PJ, inativar/reativar) e usuários (bloquear/desbloquear).
- **Raio-X (calculadora de preço mínimo):**
  - catálogo de produtos base por módulo de produção, semeado automaticamente quando um produtor é criado;
  - checklist de itens de custo;
  - simulações salvas com o resultado em semáforo (vermelho, amarelo, verde).
- **Listagens** com paginação, ordenação e busca por termo.
- **Tratamento de erros** padronizado (`ErrorResponse`) para regras de negócio, validação e recurso não encontrado.

## Arquitetura

Organização **por funcionalidade** (*package by feature*): cada domínio é uma pasta autocontida com seu Resource, Service, Repository, entidades e DTOs.

```
br.com.codenest.agro
├── auth/        login, sessões, rate limiting
├── usuario/
├── produtor/    tenant, vínculo usuário × produtor
├── papel/       papéis e permissões
├── raiox/
│   ├── produto/     catálogo de produtos base
│   ├── checklist/   itens de custo
│   └── simulacao/   simulações da calculadora
└── shared/      entidade base, tenant, paginação, exceções
```

Domínios que precisam reagir a outros usam **eventos CDI** (por exemplo, `ProdutorCriadoEvent` dispara a semeadura do catálogo do Raio-X), o que evita dependência circular entre pacotes.

O desenho multi-tenant e o que falta implementar estão em [`docs/multi-tenant.md`](docs/multi-tenant.md).

## Endpoints

| Recurso | Rotas |
|---|---|
| Autenticação | `POST /auth/login` · `POST /auth/refresh` · `POST /auth/logout` · `GET/PUT /auth/rate-limit` |
| Usuários | `GET/POST /usuarios` · `GET/PUT/DELETE /usuarios/{id}` · `PATCH /usuarios/{id}/bloquear` · `PATCH /usuarios/{id}/desbloquear` |
| Produtores | `GET/POST /produtores` · `GET/PUT /produtores/{id}` · `PATCH /produtores/{id}/inativar` · `PATCH /produtores/{id}/reativar` |
| Papéis | `GET/POST /papeis` · `GET/PUT/DELETE /papeis/{id}` · `GET /papeis/permissoes` |
| Raio-X: produtos | `GET/POST /raiox/produtos` · `GET/PUT/DELETE /raiox/produtos/{id}` · `POST /raiox/produtos/semear-padrao` |
| Raio-X: checklist | `GET/POST /raiox/checklist` · `GET/PUT/DELETE /raiox/checklist/{id}` |
| Raio-X: simulações | `GET/POST /raiox/simulacoes` · `GET/DELETE /raiox/simulacoes/{id}` |

Há uma coleção do Postman pronta em [`postman/`](postman/).

## Como rodar

### Pré-requisitos

- JDK 25
- Docker e Docker Compose

### 1. Subir o banco

```bash
docker compose up -d
```

Sobe um PostgreSQL 16 em `localhost:5432` com o banco `agro_back` (usuário e senha `agro`) e cria também o banco de testes `agro_back_test`.

### 2. Gerar o par de chaves do JWT

A chave privada não é versionada. Gere um par em `src/main/resources/`:

```bash
cd src/main/resources
openssl genrsa -out rsaPrivateKey.pem 2048
openssl pkcs8 -topk8 -nocrypt -inform pem -in rsaPrivateKey.pem -outform pem -out privateKey.pem
openssl rsa -pubout -in privateKey.pem -out publicKey.pem
rm rsaPrivateKey.pem
```

### 3. Rodar em modo dev

```bash
./mvnw quarkus:dev
```

A API sobe em `http://localhost:8090`. As migrations do Flyway rodam na inicialização e criam um super admin de bootstrap (veja `V6__seed_super_admin.sql`). Troque essa senha antes de usar em qualquer ambiente que não seja o local.

### Testes

```bash
./mvnw test
```

Os testes usam o banco `agro_back_test` criado pelo Docker Compose.

## Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `agro_back` | Nome do banco |
| `DB_USERNAME` | `agro` | Usuário do banco |
| `DB_PASSWORD` | `agro` | Senha do banco |
| `DB_SSLMODE` | `disable` | Use `require` em produção |
| `CORS_ORIGINS` | `http://localhost:4200,http://localhost:4300` | Origens liberadas, separadas por vírgula |

## Deploy

O `Dockerfile` da raiz faz o build completo dentro do Docker, que é o formato esperado por plataformas como o Render:

```bash
docker build -t agro-back .
docker run -p 8090:8090 \
  -e DB_HOST=<host> -e DB_NAME=<banco> \
  -e DB_USERNAME=<usuario> -e DB_PASSWORD=<senha> -e DB_SSLMODE=require \
  agro-back
```
