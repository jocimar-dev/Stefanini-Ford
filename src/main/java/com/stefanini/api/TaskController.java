package com.stefanini.api;

import com.stefanini.application.TaskService;
import com.stefanini.domain.Task;
import com.stefanini.domain.TaskStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task CRUD and search")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        Task created = taskService.create(toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping
    @Operation(summary = "Search tasks with optional filters and pagination")
    public Page<TaskResponse> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        TaskStatus parsedStatus = status != null && !status.isBlank() ? parseStatus(status) : null;
        LocalDateTime fromDate = parseDate(from, "from");
        LocalDateTime toDate = parseDate(to, "to");

        return taskService.search(parsedStatus, fromDate, toDate, pageable)
                .map(this::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by id")
    public TaskResponse findById(@PathVariable Long id) {
        return toResponse(taskService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task (replace all fields)")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        Task updated = taskService.update(id, toEntity(request));
        return toResponse(updated);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a task (only provided fields)")
    public TaskResponse patch(@PathVariable Long id, @RequestBody TaskPatchRequest request) {
        Task partial = toEntity(request);
        Task patched = taskService.patch(id, partial);
        return toResponse(patched);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task by id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Task toEntity(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(parseStatus(request.getStatus()));
        return task;
    }

    private Task toEntity(TaskPatchRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        TaskStatus parsedStatus = parseStatusForPatch(request.getStatus());
        task.setStatus(parsedStatus);
        return task;
    }

    private TaskStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return TaskStatus.PENDING;
        }
        try {
            return TaskStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
        }
    }

    private TaskStatus parseStatusForPatch(String status) {
        if (status == null) {
            return null;
        }
        if (status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
        }
        return parseStatus(status);
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCreatedAt(),
                task.getStatus() != null ? task.getStatus().name() : null
        );
    }

    private LocalDateTime parseDate(String input, String field) {
        if (input == null || input.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(input);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date for " + field + ": " + input);
        }
    }
}
