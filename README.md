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

## CI/CD (GitHub Actions)
`.github/workflows/ci.yml` runs `mvn clean verify`, builds Docker image, and publishes to GHCR (tags `latest` and commit SHA) on pushes to `main`.
- Releases via tag: hoje o workflow dispara só em `main` (push/PR). Se quiser build/publicar ao criar tags (ex. `v0.1.0`), ajuste o gatilho em `on:` para incluir `push: tags: ['v*']` e referencie a tag no build/push da imagem. Sem isso, criar a tag não dispara pipeline automaticamente.
- Criar release/tag localmente (exemplo): `git checkout main && git pull && git tag -a v0.1.0 -m "Release v0.1.0" && git push origin v0.1.0`.

## Git flow / commits
- Branches: `main` (production), `develop` (integra develop), `feature/<name>` (cada demanda).
- Fluxo resumido: criar branch `feature/...` a partir de `develop` -> desenvolver e commitar -> abrir PR para `develop` -> merge -> release para `main` quando pronto.
- Commits: use prefixos semanticos `feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`.
- Ajuste de commits: reescrita de historico (squash/rebase) so manualmente via Git (`git rebase -i`) e alinhado com o time; nao automatizamos aqui.

## Deploy / Infra (resumo)
- ECS Fargate com ALB + RDS (SQL Server) está descrito em `terraform/` (custa: NAT, ALB e SQL Server). Só execute `terraform apply` se quiser realmente provisionar; o `plan` é suficiente para demonstrar conhecimento.
- Para demo local sem custo, use `LOCALSTACK.md` (simula SSM/ECR) e o `docker-compose` já existente. O app roda localmente com SQL Server em contêiner; comandos `awslocal` mostram como faria param store/repos.
