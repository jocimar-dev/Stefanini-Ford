package com.stefanini.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stefanini.domain.Task;
import com.stefanini.domain.TaskStatus;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@ExtendWith(MockitoExtension.class)
class TaskEventPublisherSqsTest {

    @Mock
    private SqsClient sqsClient;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void should_publish_serialized_event_to_sqs() throws Exception {
        Task task = new Task(1L, "Title", "Description", LocalDateTime.now(), TaskStatus.PENDING);
        TaskEventPublisherSqs publisher = new TaskEventPublisherSqs(
                sqsClient,
                "http://localhost:4566/000000000000/todo-events",
                objectMapper);

        publisher.taskCreated(task);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient, times(1)).sendMessage(captor.capture());

        String body = captor.getValue().messageBody();
        JsonNode json = objectMapper.readTree(body);

        assertEquals("TASK_CREATED", json.get("eventType").asText());
        assertEquals(1L, json.get("id").asLong());
        assertEquals("Title", json.get("title").asText());
        assertNotNull(json.get("occurredAt").asText());
    }
}
