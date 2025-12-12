package com.stefanini.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final String secret = "super-secret-key-which-is-long-enough-for-hmac";

    @Test
    void shouldGenerateAndValidateToken() {
        JwtService service = new JwtService(secret, 60_000);

        String token = service.generateToken("user1");

        assertThat(service.extractUsername(token)).isEqualTo("user1");
        assertThat(service.isValid(token, "user1")).isTrue();
    }

    @Test
    void invalidSignatureShouldFail() {
        JwtService service = new JwtService(secret, 60_000);
        String token = service.generateToken("user1");

        JwtService other = new JwtService("another-secret-key-which-is-long-enough", 60_000);

        assertThatThrownBy(() -> other.isValid(token, "user1"))
                .isInstanceOf(Exception.class);
    }
}
