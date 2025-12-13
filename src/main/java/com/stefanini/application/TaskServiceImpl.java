package com.stefanini.application;

import com.stefanini.domain.Task;
import com.stefanini.domain.TaskStatus;
import com.stefanini.infrastructure.TaskRepository;
import com.stefanini.infrastructure.messaging.TaskEventPublisher;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
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
    private final TaskEventPublisher taskEventPublisher;

    public TaskServiceImpl(TaskRepository taskRepository, TaskEventPublisher taskEventPublisher) {
        this.taskRepository = taskRepository;
        this.taskEventPublisher = taskEventPublisher;
    }

    @Override
    public Task create(Task task) {
        Task created = taskRepository.save(task);
        log.info("Task created id={} title={} status={}", created.getId(), created.getTitle(), created.getStatus());
        taskEventPublisher.taskCreated(created);
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
        taskEventPublisher.taskUpdated(saved);
        return saved;
    }

    @Override
    public Task patch(Long id, Task partial) {
        Task existing = findById(id);
        if (partial.getTitle() != null) {
            existing.setTitle(partial.getTitle());
        }
        if (partial.getDescription() != null) {
            existing.setDescription(partial.getDescription());
        }
        if (partial.getStatus() != null) {
            existing.setStatus(partial.getStatus());
        }
        Task saved = taskRepository.save(existing);
        log.info("Task patched id={} title={} status={}", saved.getId(), saved.getTitle(), saved.getStatus());
        taskEventPublisher.taskPatched(saved);
        return saved;
    }

    @Override
    public void delete(Long id) {
        Task existing = findById(id);
        taskRepository.delete(existing);
        log.info("Task deleted id={} title={}", existing.getId(), existing.getTitle());
        taskEventPublisher.taskDeleted(existing);
    }
}
