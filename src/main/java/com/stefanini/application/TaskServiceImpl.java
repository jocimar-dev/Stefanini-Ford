package com.stefanini.application;

import com.stefanini.domain.Task;
import com.stefanini.domain.TaskStatus;
import com.stefanini.infrastructure.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Task create(Task task) {
        Task created = taskRepository.save(task);
        log.info("Task created id={} title={} status={}", created.getId(), created.getTitle(), created.getStatus());
        return created;
    }

    @Override
    public Page<Task> search(TaskStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Page<Task> page = taskRepository.search(status, from, to, pageable);
        log.info("Tasks fetched status={} from={} to={} page={} size={} total={}",
                status, from, to, pageable.getPageNumber(), pageable.getPageSize(), page.getTotalElements());
        return page;
    }

    @Override
    @Transactional(readOnly = true)
    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Task not found id={}", id);
                    return new EntityNotFoundException("Task not found: " + id);
                });
    }

    @Override
    public Task update(Long id, Task updated) {
        Task existing = findById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setStatus(updated.getStatus());
        Task saved = taskRepository.save(existing);
        log.info("Task updated id={} title={} status={}", saved.getId(), saved.getTitle(), saved.getStatus());
        return saved;
    }

    @Override
    public void delete(Long id) {
        Task existing = findById(id);
        taskRepository.delete(existing);
        log.info("Task deleted id={} title={}", existing.getId(), existing.getTitle());
    }
}
