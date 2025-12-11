# To-Do API

API RESTful para gerenciamento de tarefas (criar, listar, atualizar e excluir) usando Java 17, Spring Boot 3 e SQL Server.

## Requisitos
- Java 17+
- Maven Wrapper (`./mvnw`)
- Docker (opcional para subir SQL Server e rodar a API)

## Configuracao do banco
- Valores padrao em `src/main/resources/application.yml`:
  - host: `sqlserver` (docker-compose) ou `localhost`
  - banco: `todo_db`
  - usuario: `sa`
  - senha: `Ford123!`

### Subir tudo via docker-compose (API + SQL Server 2019)
```sh
docker-compose up -d --build
```
API: `http://localhost:8080/api/tasks`. Banco: `localhost:1433`.

### Subir SQL Server manualmente (Docker)
```sh
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=Ford123!" -p 1433:1433 -d mcr.microsoft.com/mssql/server:2019-latest
docker exec <container> /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Ford123!" -C -Q "CREATE DATABASE todo_db"
```
Depois rode a API:
```sh
./mvnw spring-boot:run
```
(Ajuste `spring.datasource` se mudar host/credenciais.)

## Endpoints (curl no PowerShell)
- Criar:
```powershell
curl.exe -X POST http://localhost:8080/api/tasks `
  -H "Content-Type: application/json" `
  -d "{\"title\":\"Estudar\",\"description\":\"Spring Boot\",\"status\":\"PENDING\"}"
```
- Listar: `curl.exe http://localhost:8080/api/tasks`
- Buscar por id: `curl.exe http://localhost:8080/api/tasks/1`
- Atualizar:
```powershell
curl.exe -X PUT http://localhost:8080/api/tasks/1 `
  -H "Content-Type: application/json" `
  -d "{\"title\":\"Estudar\",\"description\":\"Spring Boot 3\",\"status\":\"IN_PROGRESS\"}"
```
- Remover: `curl.exe -X DELETE http://localhost:8080/api/tasks/1`

## Testes
```sh
./mvnw test
```

## CI/CD (GitHub Actions)
Workflow em `.github/workflows/ci.yml`:
- `mvn clean verify`
- Build da imagem Docker
- Push para GHCR (tags `latest` e SHA) quando em `main`

## Deploy AWS (Terraform esqueleto)
Diretorio `terraform/` inclui:
- ECS Fargate (porta 8080)
- ALB HTTP 80 com healthcheck em `/api/tasks`
- RDS SQL Server 2019 Express (`todo_db`)
- CloudWatch Logs

Exemplo `terraform.tfvars`:
```
aws_region        = "us-east-1"
vpc_id            = "<sua-vpc>"
public_subnet_ids = ["subnet-a", "subnet-b"]
private_subnet_ids = ["subnet-c", "subnet-d"]
db_username       = "sa"
db_password       = "Ford123!"
image             = "ghcr.io/<owner>/todo-api:latest"
```
Rodar:
```sh
cd terraform
terraform init
terraform apply
```
Saidas: `alb_dns_name` (API em `http://alb_dns_name/api/tasks`) e `rds_endpoint`.

## GitFlow
- Principal: `main`
- Desenvolvimento: `develop`
- Features: `feature/<nome>`
