package com.stefanini.infrastructure.messaging;

import com.stefanini.domain.TaskStatus;
import java.time.Instant;

public class TaskEvent {
    private String eventType;
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Instant occurredAt;

    public TaskEvent(String eventType, Long id, String title, String description, TaskStatus status, Instant occurredAt) {
        this.eventType = eventType;
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.occurredAt = occurredAt;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
