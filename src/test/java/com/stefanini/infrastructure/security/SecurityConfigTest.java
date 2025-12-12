package com.stefanini.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.DummyController.class)
@Import({SecurityConfig.class, SecurityConfigTest.SecurityTestConfig.class})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = true)
class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AppUserProperties userProps;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setupProps() {
        userProps.setUsername("user");
        userProps.setPassword("pass");
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldPermitPublicEndpoints() throws Exception {
        mvc.perform(get("/actuator/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void filterChainShouldContainJwtFilter() {
        assertThat(securityFilterChain.getFilters())
                .anyMatch(f -> f instanceof JwtAuthenticationFilter);
    }

    @RestController
    static class DummyController {
        @GetMapping("/actuator/health")
        String health() {
            return "ok";
        }

        @GetMapping("/api/tasks")
        String tasks() {
            return "ok";
        }
    }

    @TestConfiguration
    static class SecurityTestConfig {
        @Bean
        AppUserProperties appUserProperties() {
            return new AppUserProperties();
        }

        // Provide a JwtService to satisfy filter dependencies if needed elsewhere
        @Bean
        JwtService jwtService() {
            return new JwtService("super-secret-key-which-is-long-enough-for-hmac", 60000);
        }
    }
}
