package com.stefanini.api;

import com.stefanini.application.TaskService;
import com.stefanini.domain.Task;
import com.stefanini.domain.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController controller;

    private LocalDateTime now;

    @BeforeEach
    void setup() {
        now = LocalDateTime.now();
    }

    @Test
    void createShouldMapRequestAndReturnResponse() {
        TaskRequest req = new TaskRequest();
        req.setTitle("Title");
        req.setDescription("Desc");
        req.setStatus("done");

        Task persisted = new Task(1L, "Title", "Desc", now, TaskStatus.DONE);
        when(taskService.create(any(Task.class))).thenReturn(persisted);

        ResponseEntity<TaskResponse> response = controller.create(req);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("DONE");

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).create(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void findAllShouldParseFiltersAndMapContent() {
        Task t1 = new Task(1L, "A", "desc", now, TaskStatus.DONE);
        Page<Task> page = new PageImpl<>(List.of(t1));
        when(taskService.search(eq(TaskStatus.DONE), any(), any(), any(Pageable.class))).thenReturn(page);

        Page<TaskResponse> result = controller.findAll("done", "2024-01-01T00:00", null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("DONE");
    }

    @Test
    void findAllShouldRejectInvalidDate() {
        assertThatThrownBy(() -> controller.findAll(null, "not-a-date", null, 0, 10))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void findAllShouldRejectInvalidStatus() {
        assertThatThrownBy(() -> controller.findAll("bad", null, null, 0, 10))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void patchShouldRejectBlankStatus() {
        TaskPatchRequest req = new TaskPatchRequest();
        req.setStatus(" ");

        assertThatThrownBy(() -> controller.patch(1L, req))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void patchWithNullStatusAllowsPartialUpdate() {
        TaskPatchRequest req = new TaskPatchRequest();
        req.setTitle("New");
        when(taskService.patch(eq(5L), any(Task.class)))
                .thenReturn(new Task(5L, "New", null, now, TaskStatus.PENDING));

        TaskResponse resp = controller.patch(5L, req);

        assertThat(resp.getTitle()).isEqualTo("New");
        verify(taskService).patch(eq(5L), any(Task.class));
    }

    @Test
    void deleteShouldReturnNoContent() {
        ResponseEntity<Void> resp = controller.delete(3L);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        verify(taskService).delete(3L);
    }
}
