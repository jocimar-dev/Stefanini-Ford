package com.stefanini.infrastructure.messaging;

import com.stefanini.domain.Task;

public interface TaskEventPublisher {

    void taskCreated(Task task);

    void taskUpdated(Task task);

    void taskPatched(Task task);

    void taskDeleted(Task task);
}
