package com.stefanini.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stefanini.domain.Task;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Publishes task events to SQS; logs and continues on failure.
 */
public class TaskEventPublisherSqs implements TaskEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TaskEventPublisherSqs.class);

    private final SqsClient sqsClient;
    private final String queueUrl;
    private final ObjectMapper objectMapper;

    public TaskEventPublisherSqs(SqsClient sqsClient, String queueUrl, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        // Use the Spring-configured mapper (with Java Time module) to avoid serialization errors.
        this.objectMapper = objectMapper;
    }

    @Override
    public void taskCreated(Task task) {
        publish("TASK_CREATED", task);
    }

    @Override
    public void taskUpdated(Task task) {
        publish("TASK_UPDATED", task);
    }

    @Override
    public void taskPatched(Task task) {
        publish("TASK_PATCHED", task);
    }

    @Override
    public void taskDeleted(Task task) {
        publish("TASK_DELETED", task);
    }

    private void publish(String eventType, Task task) {
        TaskEvent event = new TaskEvent(
                eventType,
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                Instant.now());

        try {
            String payload = objectMapper.writeValueAsString(event);
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(payload)
                    .build();
            sqsClient.sendMessage(request);
            log.info("Published event {} for task id={} to SQS", eventType, task.getId());
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize event {} for task id={}: {}", eventType, task.getId(), e.getMessage());
        } catch (Exception e) {
            log.warn("Failed to publish event {} for task id={} to SQS: {}", eventType, task.getId(), e.getMessage());
        }
    }
}
