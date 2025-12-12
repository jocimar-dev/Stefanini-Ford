package com.stefanini.infrastructure.aws;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stefanini.infrastructure.messaging.TaskEventPublisher;
import com.stefanini.infrastructure.messaging.TaskEventPublisherNoop;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

class SqsConfigTest {

    private final SqsConfig config = new SqsConfig();

    @Test
    void should_use_static_credentials_when_endpoint_overridden() {
        SqsProperties props = new SqsProperties();
        props.setEnabled(true);
        props.setRegion("us-east-1");
        props.setEndpoint("http://localhost:4566");

        try (SqsClient client = config.sqsClient(props)) {
            var provider = client.serviceClientConfiguration().credentialsProvider();
            assertTrue(provider instanceof StaticCredentialsProvider);
        }
    }

    @Test
    void should_return_noop_when_queue_url_missing() {
        SqsProperties props = new SqsProperties();
        props.setEnabled(true);
        props.setRegion("us-east-1");
        props.setEndpoint("http://localhost:4566");
        props.setQueueUrl("");

        TaskEventPublisher publisher = config.taskEventPublisherSqs(mock(SqsClient.class), props, new ObjectMapper());

        assertTrue(publisher instanceof TaskEventPublisherNoop);
    }
}
