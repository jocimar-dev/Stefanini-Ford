package com.stefanini.infrastructure.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Guarantees a TaskEventPublisher bean is always available.
 * If SQS is enabled, SqsConfig provides the publisher; otherwise we fall back to noop.
 */
@Configuration
public class MessagingConfig {

    @Bean
    @ConditionalOnMissingBean(TaskEventPublisher.class)
    TaskEventPublisher taskEventPublisherFallback() {
        return new TaskEventPublisherNoop();
    }
}
