package com.stefanini.infrastructure.aws;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.stefanini.infrastructure.messaging.TaskEventPublisher;
import com.stefanini.infrastructure.messaging.TaskEventPublisherNoop;
import com.stefanini.infrastructure.messaging.TaskEventPublisherSqs;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

/**
 * Optional SQS publisher wired only when app.aws.sqs.enabled=true.
 */
@Configuration
@EnableConfigurationProperties(SqsProperties.class)
public class SqsConfig {

    private static final Logger log = LoggerFactory.getLogger(SqsConfig.class);

    @Bean
    @ConditionalOnProperty(prefix = "app.aws.sqs", name = "enabled", havingValue = "true")
    SqsClient sqsClient(SqsProperties props) {
        SqsClientBuilder builder = SqsClient.builder()
            .region(Region.of(props.getRegion()));

        if (props.getEndpoint() != null && !props.getEndpoint().isBlank()) {
            builder = builder
                .endpointOverride(URI.create(props.getEndpoint()))
                // LocalStack aceita qualquer credencial; usar estática evita erro quando não há profile AWS configurado.
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")));
        } else {
            builder = builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.aws.sqs", name = "enabled", havingValue = "true")
    TaskEventPublisher taskEventPublisherSqs(SqsClient sqsClient, SqsProperties props, ObjectMapper objectMapper) {
        if (props.getQueueUrl() == null || props.getQueueUrl().isBlank()) {
            log.warn("SQS enabled but queue URL is empty; falling back to noop publisher");
            return new TaskEventPublisherNoop();
        }
        return new TaskEventPublisherSqs(sqsClient, props.getQueueUrl(), objectMapper);
    }
}
