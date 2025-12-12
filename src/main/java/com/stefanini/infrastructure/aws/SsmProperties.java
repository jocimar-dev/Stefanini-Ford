package com.stefanini.infrastructure.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws.ssm")
public class SsmProperties {

    /**
     * Enable SSM parameter fetching for database credentials.
     */
    private boolean enabled = false;

    /**
     * AWS region to target (used for LocalStack as well).
     */
    private String region = "us-east-1";

    /**
     * Optional endpoint override (set to http://localhost:4566 for LocalStack).
     */
    private String endpoint;

    private String dbHostParam = "/todo/db/host";
    private String dbNameParam = "/todo/db/name";
    private String dbUserParam = "/todo/db/user";
    private String dbPasswordParam = "/todo/db/password";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public String getDbHostParam() {
        return dbHostParam;
    }

    public void setDbHostParam(String dbHostParam) {
        this.dbHostParam = dbHostParam;
    }

    public String getDbNameParam() {
        return dbNameParam;
    }

    public void setDbNameParam(String dbNameParam) {
        this.dbNameParam = dbNameParam;
    }

    public String getDbUserParam() {
        return dbUserParam;
    }

    public void setDbUserParam(String dbUserParam) {
        this.dbUserParam = dbUserParam;
    }

    public String getDbPasswordParam() {
        return dbPasswordParam;
    }

    public void setDbPasswordParam(String dbPasswordParam) {
        this.dbPasswordParam = dbPasswordParam;
    }
}
