package com.stefanini.infrastructure.aws;

import java.net.URI;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.SsmClientBuilder;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

/**
 * Optional DataSource builder that pulls DB connection info from AWS SSM (or LocalStack).
 * Falls back to the existing env/application.yml values when parameters are missing or SSM is unavailable.
 */
@Configuration
@EnableConfigurationProperties(SsmProperties.class)
@ConditionalOnProperty(prefix = "app.aws.ssm", name = "enabled", havingValue = "true")
public class SsmDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(SsmDataSourceConfig.class);

    @Value("${DB_HOST:localhost}")
    private String defaultHost;

    @Value("${DB_NAME:todo_db}")
    private String defaultDb;

    @Value("${DB_USER:sa}")
    private String defaultUser;

    @Value("${DB_PASSWORD:Ford123!}")
    private String defaultPassword;

    @Bean
    SsmClient ssmClient(SsmProperties props) {
        SsmClientBuilder builder = SsmClient.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .region(Region.of(props.getRegion()));

        if (props.getEndpoint() != null && !props.getEndpoint().isBlank()) {
            builder = builder.endpointOverride(URI.create(props.getEndpoint()));
        }

        return builder.build();
    }

    @Bean
    @Primary
    DataSource dataSource(SsmClient ssmClient, SsmProperties props) {
        String host = resolveOrFallback(ssmClient, props.getDbHostParam(), false, defaultHost);
        String dbName = resolveOrFallback(ssmClient, props.getDbNameParam(), false, defaultDb);
        String user = resolveOrFallback(ssmClient, props.getDbUserParam(), false, defaultUser);
        String password = resolveOrFallback(ssmClient, props.getDbPasswordParam(), true, defaultPassword);

        String url = String.format("jdbc:sqlserver://%s:1433;databaseName=%s;encrypt=false", host, dbName);
        log.info("Initializing DataSource using SSM parameters (host={}, db={})", host, dbName);

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    private String resolveOrFallback(SsmClient client, String paramName, boolean decrypt, String fallback) {
        try {
            GetParameterRequest request = GetParameterRequest.builder()
                .name(paramName)
                .withDecryption(decrypt)
                .build();

            GetParameterResponse response = client.getParameter(request);
            String value = response.parameter().value();
            log.debug("Fetched parameter {} from SSM", paramName);
            return value;
        } catch (Exception ex) {
            log.warn("Could not fetch SSM parameter {}. Using fallback value. Cause: {}", paramName, ex.getMessage());
            return fallback;
        }
    }
}
