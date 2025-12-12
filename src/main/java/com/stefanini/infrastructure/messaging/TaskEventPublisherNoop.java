package com.stefanini.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import com.stefanini.domain.Task;

/**
 * Default publisher that does nothing. It is replaced when SQS is enabled.
 */
@Component
@ConditionalOnMissingBean(TaskEventPublisher.class)
public class TaskEventPublisherNoop implements TaskEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TaskEventPublisherNoop.class);

    @Override
    public void taskCreated(Task task) {
        log.debug("Noop publisher - taskCreated id={}", task.getId());
    }

    @Override
    public void taskUpdated(Task task) {
        log.debug("Noop publisher - taskUpdated id={}", task.getId());
    }

    @Override
    public void taskPatched(Task task) {
        log.debug("Noop publisher - taskPatched id={}", task.getId());
    }

    @Override
    public void taskDeleted(Task task) {
        log.debug("Noop publisher - taskDeleted id={}", task.getId());
    }
}
