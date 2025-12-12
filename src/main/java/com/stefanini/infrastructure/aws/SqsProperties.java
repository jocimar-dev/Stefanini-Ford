package com.stefanini.infrastructure.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws.sqs")
public class SqsProperties {

    /**
     * Enable publishing to SQS/LocalStack.
     */
    private boolean enabled = false;

    /**
     * Queue URL (e.g., http://localhost:4566/000000000000/todo-events on LocalStack).
     */
    private String queueUrl;

    /**
     * AWS region (or LocalStack region).
     */
    private String region = "us-east-1";

    /**
     * Optional endpoint override (http://localhost:4566 for LocalStack).
     */
    private String endpoint;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
