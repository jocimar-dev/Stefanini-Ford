# To-Do API

RESTful API for task management (create, list, update, partial update, delete) using Java 17, Spring Boot 3, SQL Server, JPA, JWT, and Docker.

## Requirements
- Java 17+
- Maven (`mvn`)
- Docker + Docker Compose (optional for SQL Server/API)

## Configuration
Defaults in `src/main/resources/application.yml` (override via env vars):
- DB host: `${DB_HOST:localhost}` (compose: `sqlserver`)
- DB name: `${DB_NAME:todo_db}`
- DB user: `${DB_USER:sa}`
- DB password: `${DB_PASSWORD:Ford123!}`
- JWT: `app.security.jwt.*`
- Default user: `admin` / `admin123`

## Running
1) Start SQL Server via Compose: `docker compose up -d sqlserver`
2) Ensure DB exists: `docker exec todo-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Ford123!" -C -Q "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name='todo_db') CREATE DATABASE todo_db;"`
3) Run API locally: `mvn spring-boot:run`
   - Flyway está habilitado (`spring.flyway.enabled=true`) e aplica as migrations automaticamente na inicialização; basta que o banco `todo_db` exista.
4) (Optional) All via Compose: `docker compose up -d --build`

API: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`
Actuator (monitoring): `http://localhost:8080/actuator` (health is public; others require Bearer token)

## Auth (JWT)
- Login: `POST /api/auth/login` body `{"username":"admin","password":"admin123"}`
- Response: `{"token":"<JWT>"}`
- Use header `Authorization: Bearer <JWT>` on all protected endpoints.
- No Swagger UI, clique em "Authorize", escolha `bearerAuth` e informe o token nesse formato: `Bearer <JWT>`.

## Endpoints (curl examples)
- Login (get token):
```sh
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
- Create task (example using token):
```sh
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{"title":"Study","description":"Spring Boot","status":"PENDING"}'
```
Use Swagger UI for the rest of the operations (list, get by id, put, patch, delete) with the same `Authorization: Bearer <JWT>` header.

## Actuator
- Exposed endpoints: `health` (public), `info`, `metrics`, `env`, `httpexchanges`, `threaddump` (require JWT).
- Base URL: `http://localhost:8080/actuator`
- Example with token:  
  `curl http://localhost:8080/actuator/metrics -H "Authorization: Bearer <JWT>"`

## Logs
- API: `docker compose logs -f todo-api`
- DB: `docker compose logs -f sqlserver`

## Tests
```sh
mvn test
```

Requer Docker em execução para os testes de integração (Testcontainers com SQL Server).

## CI/CD (GitHub Actions)
- `Main CI - Build & Publish` (`.github/workflows/ci.yml`): executa `mvn clean verify`, builda imagem e publica `latest` + SHA no GHCR em pushes para `main`.
- `Dev CI - Build & Publish` (`.github/workflows/dev.yml`): mesmo fluxo para `develop`, publica tags `dev` e `dev-<sha>`.
- `Release - Tag Build & Publish` (`.github/workflows/release.yml`): dispara em tags `v*`, builda e publica imagem `:vX` e `:latest`.

### Release (Gitflow)
1) Merge da feature no `develop`.
2) Criar branch de release a partir do develop:
   - `git switch -c release/v1.0.0 develop`
   - `git push -u origin release/v1.0.0`
3) Abrir PR `release/v1.0.0` -> `main` e fazer merge.
4) Abrir PR `release/v1.0.0` -> `develop` para reintegrar.
5) Tag na `main` (aciona workflow de release):
   - `git tag v1.0.0`
   - `git push origin v1.0.0`
6) Atualizar branches locais:
   - `git switch develop && git pull`
   - `git switch main && git pull`

## Git flow / commits
- Branches: `main` (production), `develop` (integra develop), `feature/<name>` (cada demanda).
- Fluxo resumido: criar branch `feature/...` a partir de `develop` -> desenvolver e commitar -> abrir PR para `develop` -> merge -> release para `main` quando pronto.
- Commits: use prefixos semanticos `feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`.
- Ajuste de commits: reescrita de historico (squash/rebase) so manualmente via Git (`git rebase -i`) e alinhado com o time; nao automatizamos aqui.

## Deploy / Infra (resumo)
- ECS Fargate com ALB + RDS (SQL Server) está descrito em `terraform/` (custa: NAT, ALB e SQL Server). Só execute `terraform apply` se quiser realmente provisionar; o `plan` é suficiente para demonstrar conhecimento.
- Para demo local sem custo, use `LOCALSTACK.md` (simula SSM/ECR) e o `docker-compose` já existente. O app roda localmente com SQL Server em contêiner; comandos `awslocal` mostram como faria param store/repos.
