# To-Do API

RESTful API for task management (create, list, update, delete) using Java 17, Spring Boot 3, SQL Server, Flyway, and Docker.

## Requirements
- Java 17+
- Maven Wrapper (`./mvnw`)
- Docker (optional for running SQL Server and the API)

## Project structure / architecture
- Layers follow a clean-ish separation:
  - `api` (controllers, DTOs)
  - `application` (services/use cases)
  - `domain` (entities, enums)
  - `infrastructure` (repositories)
- Persistence: JPA + SQL Server, schema managed by Flyway migrations (`src/main/resources/db/migration`).
- REST: Spring Web, validation on DTOs.

## Database config
- Defaults in `src/main/resources/application.yml`:
  - host: `sqlserver` (docker-compose) or `localhost`
  - database: `todo_db`
  - user: `sa`
  - password: `Ford123!`

### Run everything via docker-compose (API + SQL Server 2019)
```sh
docker-compose up -d --build
```
API: `http://localhost:8080/api/tasks`  
DB: `localhost:1433`

### Run SQL Server manually (Docker)
```sh
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=Ford123!" -p 1433:1433 -d mcr.microsoft.com/mssql/server:2019-latest
docker exec <container> /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Ford123!" -C -Q "CREATE DATABASE todo_db"
```

### Run the API locally (no Docker)
```sh
./mvnw spring-boot:run
```
Ensure SQL Server is reachable; adjust `spring.datasource` if host/credentials change.

## Endpoints (curl on PowerShell)
- Create
```powershell
curl.exe -X POST http://localhost:8080/api/tasks `
  -H "Content-Type: application/json" `
  -d "{\"title\":\"Study\",\"description\":\"Spring Boot\",\"status\":\"PENDING\"}"
```
- List: `curl.exe http://localhost:8080/api/tasks`
- Get by id: `curl.exe http://localhost:8080/api/tasks/1`
- Update
```powershell
curl.exe -X PUT http://localhost:8080/api/tasks/1 `
  -H "Content-Type: application/json" `
  -d "{\"title\":\"Study\",\"description\":\"Spring Boot 3\",\"status\":\"IN_PROGRESS\"}"
```
- Delete: `curl.exe -X DELETE http://localhost:8080/api/tasks/1`

### Linux note
- Use `curl` (without `.exe`) and replace backticks with backslashes:
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Study","description":"Spring Boot","status":"PENDING"}'
```
- `docker-compose` commands are the same.
- For passwords with `!`, wrap in single quotes or escape (`Ford123!` -> `'Ford123!'`).

### Logs and checks
- Tail API logs: `docker-compose logs -f todo-api`
- Tail DB logs: `docker-compose logs -f sqlserver`
- Healthcheck (ALB/ECS): `/api/tasks`
- Quick DB check inside container:  
  `docker exec todo-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'Ford123!' -C -Q "SELECT name FROM sys.databases"`

## Tests
```sh
./mvnw test
```

## CI/CD (GitHub Actions)
Workflow `.github/workflows/ci.yml`:
- `mvn clean verify`
- Build Docker image
- Push to GHCR (tags `latest` and commit SHA) on branch `main`

## Deploy on AWS (Terraform skeleton)
Folder `terraform/` includes:
- ECS Fargate (API container on port 8080)
- ALB HTTP 80 with healthcheck at `/api/tasks`
- RDS SQL Server 2019 Express (`todo_db`)
- CloudWatch Logs

Example `terraform.tfvars`:
```
aws_region        = "us-east-1"
vpc_id            = "<your-vpc>"
public_subnet_ids = ["subnet-a", "subnet-b"]
private_subnet_ids = ["subnet-c", "subnet-d"]
db_username       = "sa"
db_password       = "Ford123!"
image             = "ghcr.io/<owner>/todo-api:latest"
```
Run:
```sh
cd terraform
terraform init
terraform apply
```
Outputs: `alb_dns_name` (API at `http://alb_dns_name/api/tasks`) and `rds_endpoint`.

## GitFlow
- Main: `main`
- Development: `develop`
- Features: `feature/<name>`

## Commit messages (semantic)
- Use prefixos como `feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:` para facilitar o histórico e automação.
