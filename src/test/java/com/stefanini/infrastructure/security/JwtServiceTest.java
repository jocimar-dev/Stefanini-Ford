package com.stefanini.infrastructure.security;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

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
    void shouldFailWhenExpired() {
        Clock fixed = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
        JwtService service = new JwtService(secret, 1, fixed);

        String token = service.generateToken("user1");

        Clock later = Clock.fixed(Instant.parse("2024-01-01T00:00:02Z"), ZoneOffset.UTC);
        JwtService validator = new JwtService(secret, 1, later);

        assertThat(validator.isValid(token, "user1")).isFalse();
    }

    @Test
    void invalidSignatureShouldFail() {
        JwtService service = new JwtService(secret, 60_000);
        String token = service.generateToken("user1");

        JwtService other = new JwtService("another-secret-key-which-is-long-enough", 60_000);

        assertThat(other.isValid(token, "user1")).isFalse();
    }
}
