# To-Do API

API RESTful para gerenciamento de tarefas (criar, listar, atualizar e excluir) usando Java 17, Spring Boot 3 e SQL Server.

## Requisitos
- Java 17+
- Maven Wrapper (`./mvnw`) incluído no projeto
- SQL Server (local ou container)

## Configuração do banco
1. Suba o SQL Server localmente (exemplo via Docker):
   ```sh
   docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=YourStrong(!)Password" -p 1433:1433 -d mcr.microsoft.com/mssql/server:2022-latest
   ```
2. Crie o banco `todo_db` (ou ajuste o `databaseName` no `application.yml`):
   ```sh
   docker exec -it <container> /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "YourStrong(!)Password" -Q "CREATE DATABASE todo_db"
   ```
3. Configure credenciais no `src/main/resources/application.yml` se necessário.
4. Flyway cria a tabela `tasks` na inicialização.

## Como rodar a aplicação
```sh
./mvnw spring-boot:run
```

## Endpoints
- `POST /api/tasks` — cria tarefa  
  Body exemplo:
  ```json
  {"title": "Estudar", "description": "Spring Boot", "status": "PENDING"}
  ```
- `GET /api/tasks` — lista todas
- `GET /api/tasks/{id}` — busca por id
- `PUT /api/tasks/{id}` — atualiza tarefa  
  Body exemplo:
  ```json
  {"title": "Estudar", "description": "Spring Boot 3", "status": "IN_PROGRESS"}
  ```
- `DELETE /api/tasks/{id}` — remove tarefa

## Testes
```sh
./mvnw test
```
