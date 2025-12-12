# Demo local com LocalStack (sem custos na AWS)

Use este fluxo para demonstrar uma configuração “parecida com AWS” localmente, sem provisionar nada na nuvem. Foco: segredos/parametrização e repositório de imagens; ECS/ALB/Fargate não são bem simulados na edição community do LocalStack, então o app roda via `docker-compose`.

## Pré-requisitos
- Docker
- AWS CLI ou `awslocal` (`pip install awscli-local`)
- O repo já possui `docker-compose.yml` (app + SQL Server local).

## Passos
1) Subir o LocalStack com serviços mínimos:
```bash
docker run -d --name localstack \
  -p 4566:4566 -p 4510-4559:4510-4559 \
  -e SERVICES=ecr,ssm,secretsmanager \
  -e DEFAULT_REGION=us-east-1 \
  localstack/localstack:latest
```
2) (Opcional) Criar um “fake ECR” e armazenar segredos:
```bash
# SSM Parameters (exemplo)
awslocal ssm put-parameter --name /todo/db/host --value sqlserver --type String
awslocal ssm put-parameter --name /todo/db/name --value todo_db --type String
awslocal ssm put-parameter --name /todo/db/user --value sa --type String
awslocal ssm put-parameter --name /todo/db/password --value Ford123! --type SecureString

# ECR repo (para demonstrar comandos; não é necessário para rodar o compose)
awslocal ecr create-repository --repository-name todo-api
```
Observação: ECR/registry no LocalStack community é apenas para demonstrar comandos; você pode seguir com o build/push, mas o app local não depende disso.
3) Rodar o app localmente (usa o SQL Server do compose, não o RDS):
```bash
docker-compose up --build
```
- API em `http://localhost:8080`
- DB em `localhost:1433` (container `sqlserver`)
4) Testar rapidamente
- Saúde: `curl http://localhost:8080/actuator/health`
- Listar tarefas (se endpoint exposto): `curl http://localhost:8080/api/tasks`
- Ver logs: `docker logs todo-api`
5) Explicar na entrevista
- Como mapeou segredos/params no SSM (com `awslocal`).
- Como criaria ECR e faria push (com `awslocal ecr ...`), mesmo que não esteja usando para rodar o app.
- Por que ECS/ALB/Fargate não estão no demo local (serviços gerenciados não são bem emulados) e que, na nuvem, usaria os mesmos conceitos: imagem no ECR, segredos no SSM/Secrets, serviço ECS com SG/ALB.

## Limpeza
```bash
docker stop localstack && docker rm localstack
docker-compose down
```
