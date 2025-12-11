package com.stefanini.infrastructure;

import com.stefanini.domain.Task;
import com.stefanini.domain.TaskStatus;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
            select t from Task t
            where (:status is null or t.status = :status)
              and (:from is null or t.createdAt >= :from)
              and (:to is null or t.createdAt <= :to)
            """)
    Page<Task> search(TaskStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
