package com.stefanini.application;

import com.stefanini.domain.Task;
import java.util.List;

public interface TaskService {

    Task create(Task task);

    List<Task> findAll();

    Task findById(Long id);

    Task update(Long id, Task updated);

    void delete(Long id);
}
