package com.stefanini.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskControllerIT {

    private static final String DB_NAME = "todo_db";

    @Container
    private static final MSSQLServerContainer<?> sqlserver = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense()
            .withPassword("Password123!");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void setupDatabase() throws Exception {
        createDatabase();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> String.format(
                "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false",
                sqlserver.getHost(), sqlserver.getMappedPort(1433), DB_NAME));
        registry.add("spring.datasource.username", sqlserver::getUsername);
        registry.add("spring.datasource.password", sqlserver::getPassword);
    }

    @Test
    @DisplayName("Fluxo completo: autenticar, criar e listar tarefa")
    void shouldCreateAndListTasks() {
        String baseUrl = "http://localhost:" + port;

        String token = authenticate(baseUrl);

        TaskRequest createReq = new TaskRequest();
        createReq.setTitle("Teste integração");
        createReq.setDescription("Criada via Testcontainers");
        createReq.setStatus("PENDING");

        HttpHeaders headers = bearerHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<TaskResponse> created = restTemplate.exchange(
                baseUrl + "/api/tasks",
                HttpMethod.POST,
                new HttpEntity<>(createReq, headers),
                TaskResponse.class);

        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().getId()).isNotNull();
        assertThat(created.getBody().getTitle()).isEqualTo("Teste integração");

        ResponseEntity<PageResponse<TaskResponse>> list = restTemplate.exchange(
                baseUrl + "/api/tasks",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {});

        assertThat(list.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(list.getBody()).isNotNull();
        assertThat(list.getBody().content()).isNotEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar requisições sem token e com token inválido")
    void shouldRejectWithoutOrWithInvalidToken() {
        String baseUrl = "http://localhost:" + port;

        // Sem token
        ResponseEntity<String> noAuth = restTemplate.getForEntity(baseUrl + "/api/tasks", String.class);
        assertThat(noAuth.getStatusCode().value()).isEqualTo(401);

        // Token inválido
        HttpHeaders bad = bearerHeaders("bad-token");
        ResponseEntity<String> badAuth = restTemplate.exchange(
                baseUrl + "/api/tasks",
                HttpMethod.GET,
                new HttpEntity<>(bad),
                String.class);
        assertThat(badAuth.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios e retornar 400")
    void shouldValidateRequiredFields() {
        String baseUrl = "http://localhost:" + port;
        String token = authenticate(baseUrl);

        TaskRequest invalid = new TaskRequest();
        invalid.setTitle(""); // título vazio

        HttpHeaders headers = bearerHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ApiError> resp = restTemplate.exchange(
                baseUrl + "/api/tasks",
                HttpMethod.POST,
                new HttpEntity<>(invalid, headers),
                ApiError.class);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getError()).containsIgnoringCase("Validation");
    }

    @Test
    @DisplayName("Deve filtrar por status e permitir PUT/PATCH/DELETE")
    void shouldFilterAndUpdateAndDelete() {
        String baseUrl = "http://localhost:" + port;
        String token = authenticate(baseUrl);
        HttpHeaders headers = bearerHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Cria duas tarefas com status diferentes
        TaskResponse pending = createTask(baseUrl, headers, "Task pendente", "desc", "PENDING");
        TaskResponse done = createTask(baseUrl, headers, "Task done", "desc", "DONE");

        // Filtra por DONE
        ResponseEntity<PageResponse<TaskResponse>> filtered = restTemplate.exchange(
                baseUrl + "/api/tasks?status=DONE",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {});

        assertThat(filtered.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(filtered.getBody()).isNotNull();
        assertThat(filtered.getBody().content()).extracting(TaskResponse::getStatus).containsOnly("DONE");

        // PUT altera título e status
        TaskRequest updateReq = new TaskRequest();
        updateReq.setTitle("Atualizada");
        updateReq.setDescription("desc");
        updateReq.setStatus("IN_PROGRESS");

        ResponseEntity<TaskResponse> updated = restTemplate.exchange(
                baseUrl + "/api/tasks/" + pending.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateReq, headers),
                TaskResponse.class);

        assertThat(updated.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(updated.getBody()).isNotNull();
        assertThat(updated.getBody().getTitle()).isEqualTo("Atualizada");
        assertThat(updated.getBody().getStatus()).isEqualTo("IN_PROGRESS");

        // PATCH altera apenas status
        TaskPatchRequest patchReq = new TaskPatchRequest();
        patchReq.setStatus("DONE");
        ResponseEntity<TaskResponse> patched = restTemplate.exchange(
                baseUrl + "/api/tasks/" + pending.getId(),
                HttpMethod.PATCH,
                new HttpEntity<>(patchReq, headers),
                TaskResponse.class);
        assertThat(patched.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(patched.getBody()).isNotNull();
        assertThat(patched.getBody().getStatus()).isEqualTo("DONE");

        // DELETE e garante 404 depois
        ResponseEntity<Void> deleted = restTemplate.exchange(
                baseUrl + "/api/tasks/" + pending.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class);
        assertThat(deleted.getStatusCode().value()).isEqualTo(204);

        ResponseEntity<ApiError> afterDelete = restTemplate.exchange(
                baseUrl + "/api/tasks/" + pending.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ApiError.class);
        assertThat(afterDelete.getStatusCode().value()).isEqualTo(404);
    }

    private String authenticate(String baseUrl) {
        LoginRequest login = new LoginRequest();
        login.setUsername("admin");
        login.setPassword("admin123");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                login,
                AuthResponse.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        return response.getBody().getToken();
    }

    private static void createDatabase() throws Exception {
        String createDbSql = String.format("IF NOT EXISTS (SELECT name FROM sys.databases WHERE name='%s') CREATE DATABASE %s;", DB_NAME, DB_NAME);
        sqlserver.execInContainer(
                "/opt/mssql-tools18/bin/sqlcmd",
                "-S", "localhost",
                "-U", sqlserver.getUsername(),
                "-P", sqlserver.getPassword(),
                "-C",
                "-Q", createDbSql
        );
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return headers;
    }

    private TaskResponse createTask(String baseUrl, HttpHeaders headers, String title, String desc, String status) {
        TaskRequest req = new TaskRequest();
        req.setTitle(title);
        req.setDescription(desc);
        req.setStatus(status);

        ResponseEntity<TaskResponse> created = restTemplate.exchange(
                baseUrl + "/api/tasks",
                HttpMethod.POST,
                new HttpEntity<>(req, headers),
                TaskResponse.class);
        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        return created.getBody();
    }

    /**
     * DTO para desserializar respostas paginadas do Spring Data REST/Springdoc
     * sem depender do PageImpl diretamente.
     */
    public record PageResponse<T>(
            @JsonProperty("content") List<T> content,
            @JsonProperty("totalElements") long totalElements,
            @JsonProperty("totalPages") int totalPages,
            @JsonProperty("number") int number,
            @JsonProperty("size") int size
    ) { }
}
