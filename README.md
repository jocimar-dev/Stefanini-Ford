# To-Do API

API RESTful para gerenciamento de tarefas (criar, listar, atualizar, atualizar parcialmente, excluir) desenvolvida com Java 17 e Spring Boot 3. Utiliza SQL Server (JPA/Flyway), segurança JWT, Docker/Compose para execução local, LocalStack (SSM/SQS) opcional para demonstração em nuvem e Terraform para infraestrutura AWS.

## Indice
- [Requisitos](#requisitos)
- [Configuracao](#configuracao)
- [Como rodar](#como-rodar)
- [Autenticacao (JWT)](#autenticacao-jwt)
- [Endpoints principais](#endpoints-principais)
- [Actuator](#actuator)
- [Logs](#logs)
- [Testes](#testes)
- [CI/CD](#cicd)
- [Git flow / commits](#git-flow--commits)
- [Cloud opcional (LocalStack)](#cloud-opcional-localstack)
- [Infra (resumo)](#infra-resumo)

## Requisitos
- Java 17+
-- Maven
- Docker + Docker Compose (para SQL Server/API)

## Configuracao
Valores padrao em `src/main/resources/application.yml` (sobreponha via env vars):
- DB host: `${DB_HOST:localhost}` (compose: `sqlserver`)
- DB name: `${DB_NAME:todo_db}`
- DB user: `${DB_USER:sa}`
- DB password: `${DB_PASSWORD:Ford123!}`
- JWT: `app.security.jwt.*`
- Default user: `admin` / `admin123`
- Cloud opcional: SSM/SQS com LocalStack em `docs/LOCALSTACK_TESTING.md`.

## Como rodar
1) Subir SQL Server via Compose:
```bash
docker compose up -d sqlserver
```
2) Garantir DB existe:
```bash
docker exec todo-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Ford123!" -C -Q "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name='todo_db') CREATE DATABASE todo_db;"
```
3) Rodar API local:
```bash
mvn spring-boot:run
```
   - Flyway esta habilitado (`spring.flyway.enabled=true`) e aplica migrations automaticamente; basta o banco `todo_db`.
4) (Opcional) Tudo via Compose:
```bash
docker compose up -d --build
```

API: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`  
Actuator: `http://localhost:8080/actuator` (health publico; demais exigem JWT)

## Autenticacao (JWT)
- Login: `POST /api/auth/login` body `{"username":"admin","password":"admin123"}`
- Resposta: `{"token":"<JWT>"}`
- Use `Authorization: Bearer <JWT>` em endpoints protegidos.
- No Swagger UI clique em "Authorize" > `bearerAuth` e informe `Bearer <JWT>`.

## Endpoints principais
- Login (token):
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
- Criar tarefa (com token):
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{"title":"Study","description":"Spring Boot","status":"PENDING"}'
```
Use a Swagger UI para listar, buscar, atualizar e excluir tarefas.

## Actuator
- Expostos: `health` (publico), `info`, `metrics`, `env`, `httpexchanges`, `threaddump` (com JWT).
- Base: `http://localhost:8080/actuator`
- Exemplo com token:
```bash
curl http://localhost:8080/actuator/metrics -H "Authorization: Bearer <JWT>"
```

## Logs
- API:
```bash
docker compose logs -f todo-api
```
- DB:
```bash
docker compose logs -f sqlserver
```

## Testes
```bash
mvn test
```
Requer Docker em execucao para os testes de integracao (Testcontainers com SQL Server).

## CI/CD
- `Main CI - Build & Publish` (`.github/workflows/ci.yml`): `mvn clean verify`, build e push para GHCR em `main`.
- `Dev CI - Build & Publish` (`.github/workflows/dev.yml`): mesmo fluxo para `develop`, publica tags `dev` e `dev-<sha>`.
- `Release - Tag Build & Publish` (`.github/workflows/release.yml`): em tags `v*`, builda e publica imagens `:vX` e `:latest`. O deploy para produção exige aprovação manual do aprovador configurado no ambiente protegido no GitHub Actions; sem aprovação, a publicação não prossegue.

## Git flow / commits

| Item      | Descrição                                                                 |
|-----------|---------------------------------------------------------------------------|
| Branches  | `main` (producao), `develop` (integracao), `feature/<name>` (demanda), `release/<version>` (pre-release) |
| Fluxo     | Criar branch a partir de `develop` -> desenvolver/commitar -> PR para `develop` -> merge -> release para `main` quando pronto |
| Commits   | Prefixos `feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`        |
| Releases  | `release/vX.Y.Z` a partir de `develop`, PR para `main` e `develop`, tag `vX.Y.Z` na `main` |

## Cloud opcional (LocalStack)
Guia completo em [`docs/LOCALSTACK_TESTING.md`](docs/LOCALSTACK_TESTING.md) (subir LocalStack, credenciais dummy, defaults no `application.yml`, comandos `awslocal` para SSM/SQS).

## Infra resumo
- Terraform em `terraform/` descreve ECS Fargate + ALB + RDS (SQL Server). Gera custo real (NAT/ALB/RDS); use `terraform plan` para demonstrar, aplique apenas se quiser provisionar.
- Ambiente local sem custo: `docker-compose.yml` para API + SQL Server. LocalStack opcional para SSM/SQS (veja `LOCALSTACK_TESTING.md`).
