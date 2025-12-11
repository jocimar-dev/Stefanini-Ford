package com.stefanini.application;

import com.stefanini.domain.Task;
import com.stefanini.domain.TaskStatus;
import com.stefanini.infrastructure.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    @DisplayName("Deve criar uma tarefa")
    void shouldCreateTask() {
        Task toSave = new Task(null, "Title", "Desc", null, TaskStatus.PENDING);
        Task saved = new Task(1L, "Title", "Desc", null, TaskStatus.PENDING);

        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        Task result = taskService.create(toSave);

        assertNotNull(result.getId());
        assertEquals("Title", result.getTitle());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Deve buscar tarefa por id existente")
    void shouldFindById() {
        Task existing = new Task(1L, "Title", "Desc", null, TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(eq(1L))).thenReturn(Optional.of(existing));

        Task result = taskService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar id inexistente")
    void shouldThrowWhenNotFound() {
        when(taskRepository.findById(eq(99L))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.findById(99L));
    }
}
