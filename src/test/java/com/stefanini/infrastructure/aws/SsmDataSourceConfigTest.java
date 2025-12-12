package com.stefanini.infrastructure.aws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

@ExtendWith(MockitoExtension.class)
class SsmDataSourceConfigTest {

    @Mock
    private SsmClient ssmClient;

    private SsmDataSourceConfig config;
    private SsmProperties props;

    @BeforeEach
    void setUp() {
        config = new SsmDataSourceConfig();
        props = new SsmProperties();
        // Inject defaults so we can assert fallbacks without spinning a Spring context.
        ReflectionTestUtils.setField(config, "defaultHost", "localhost");
        ReflectionTestUtils.setField(config, "defaultDb", "todo_db");
        ReflectionTestUtils.setField(config, "defaultUser", "sa");
        ReflectionTestUtils.setField(config, "defaultPassword", "Ford123!");
    }

    @Test
    void should_build_datasource_with_ssm_values() {
        when(ssmClient.getParameter(any(GetParameterRequest.class)))
            .thenReturn(param("host-ssm"), param("db-ssm"), param("user-ssm"), param("pass-ssm"));

        DataSource dataSource = config.dataSource(ssmClient, props);
        DriverManagerDataSource ds = (DriverManagerDataSource) dataSource;

        assertEquals("jdbc:sqlserver://host-ssm:1433;databaseName=db-ssm;encrypt=false", ds.getUrl());
        assertEquals("user-ssm", ds.getUsername());
        assertEquals("pass-ssm", ds.getPassword());
    }

    @Test
    void should_fallback_to_defaults_when_ssm_fails() {
        when(ssmClient.getParameter(any(GetParameterRequest.class)))
            .thenReturn(param("host-ssm"), param("db-ssm"), param("user-ssm"))
            .thenThrow(new RuntimeException("SSM unavailable"));

        DataSource dataSource = config.dataSource(ssmClient, props);
        DriverManagerDataSource ds = (DriverManagerDataSource) dataSource;

        assertEquals("jdbc:sqlserver://host-ssm:1433;databaseName=db-ssm;encrypt=false", ds.getUrl());
        assertEquals("user-ssm", ds.getUsername());
        assertEquals("Ford123!", ds.getPassword());
    }

    private GetParameterResponse param(String value) {
        return GetParameterResponse.builder()
            .parameter(Parameter.builder().value(value).build())
            .build();
    }
}
