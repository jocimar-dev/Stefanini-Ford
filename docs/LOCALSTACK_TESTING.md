# Testes opcionais com LocalStack (SSM/SQS)

Este guia é apenas um plus para demonstrar integrações cloud sem custo. A aplicação roda sem LocalStack; use apenas se quiser testar SSM/SQS localmente.

## Pré-requisitos
- Docker (para subir o LocalStack e o SQL Server do compose)
- `awslocal` (AWS CLI + wrapper) instalado no host

## Subir LocalStack e fila SQS
```powershell
docker compose up -d localstack init-localstack
```
Isso cria automaticamente a fila `todo-events`. Se quiser conferir:
```powershell
$env:AWS_PROFILE=""
$env:AWS_ACCESS_KEY_ID="test"
$env:AWS_SECRET_ACCESS_KEY="test"
$env:AWS_DEFAULT_REGION="us-east-1"
awslocal sqs list-queues
```

## Variáveis para rodar a API com SQS (defaults já no application.yml)
Apenas ligue o recurso:
```powershell
$env:APP_AWS_SQS_ENABLED="true"
mvn spring-boot:run
```
Caso precise trocar algo, os defaults são:
- `APP_AWS_SQS_ENDPOINT` (default `http://localhost:4566`)
- `APP_AWS_SQS_REGION` (default `us-east-1`)
- `APP_AWS_SQS_QUEUE_URL` (default `http://localhost:4566/000000000000/todo-events`)

## Testar publicação
1) Crie/atualize/exclua uma tarefa (via Swagger/curl).
2) Leia a fila:
```powershell
awslocal sqs receive-message --queue-url http://localhost:4566/000000000000/todo-events --wait-time-seconds 5
```

## SSM (opcional)
Se quiser ler credenciais de DB via SSM:
```powershell
awslocal ssm put-parameter --name /todo/db/host --value sqlserver --type String
awslocal ssm put-parameter --name /todo/db/name --value todo_db --type String
awslocal ssm put-parameter --name /todo/db/user --value sa --type String
awslocal ssm put-parameter --name /todo/db/password --value Ford123! --type SecureString
$env:APP_AWS_SSM_ENABLED="true"
mvn spring-boot:run
```
Defaults no `application.yml`:
- endpoint `http://localhost:4566`
- region `us-east-1`
- params `/todo/db/*`
