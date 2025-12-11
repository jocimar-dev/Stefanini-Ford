package com.stefanini.application;

import com.stefanini.domain.Task;
import com.stefanini.domain.TaskStatus;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    Task create(Task task);

    Page<Task> search(TaskStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Task findById(Long id);

    Task update(Long id, Task updated);

    void delete(Long id);
}
