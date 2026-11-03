package com.example.authpoc.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("john", "john@example.com", "hash");
    }

    @Test
    @DisplayName("new user should default to USER role and enabled")
    void newUser_ShouldHaveSafeDefaults() {
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getLastLoginAt()).isNull();
    }

    @Test
    @DisplayName("recordLogin should set the last login timestamp")
    void recordLogin_ShouldSetLastLoginAt() {
        user.recordLogin(NOW);

        assertThat(user.getLastLoginAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("matchesPassword should compare against the stored hash, not the raw password")
    void matchesPassword_ShouldUseStoredHash() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.matches("raw", "hash")).thenReturn(true);
        when(encoder.matches("wrong", "hash")).thenReturn(false);

        assertThat(user.matchesPassword("raw", encoder)).isTrue();
        assertThat(user.matchesPassword("wrong", encoder)).isFalse();
    }
}
