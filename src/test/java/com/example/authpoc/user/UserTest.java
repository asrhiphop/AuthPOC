package com.example.authpoc.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserTest {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration LOCKOUT = Duration.ofMinutes(15);
    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
    private static final Instant LOCKOUT_EXPIRES_AT = NOW.plus(LOCKOUT);

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("john", "john@example.com", "hash");
    }

    @Test
    @DisplayName("new user should default to USER role, enabled, unlocked and without failed attempts")
    void newUser_ShouldHaveSafeDefaults() {
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isLocked(NOW)).isFalse();
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLastLoginAt()).isNull();
    }

    @Test
    @DisplayName("incrementFailedAttempts should count attempts without locking before the maximum")
    void incrementFailedAttempts_BelowMax_ShouldNotLock() {
        user.incrementFailedAttempts(MAX_ATTEMPTS, LOCKOUT, NOW);
        user.incrementFailedAttempts(MAX_ATTEMPTS, LOCKOUT, NOW);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(2);
        assertThat(user.isLocked(NOW)).isFalse();
        assertThat(user.getLockoutUntil()).isNull();
    }

    @Test
    @DisplayName("incrementFailedAttempts should lock the account when the maximum is reached")
    void incrementFailedAttempts_ReachingMax_ShouldLock() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            user.incrementFailedAttempts(MAX_ATTEMPTS, LOCKOUT, NOW);
        }

        assertThat(user.getFailedLoginAttempts()).isEqualTo(MAX_ATTEMPTS);
        assertThat(user.getLockoutUntil()).isEqualTo(LOCKOUT_EXPIRES_AT);
        assertThat(user.isLocked(LOCKOUT_EXPIRES_AT.minusMillis(1))).isTrue();
        assertThat(user.isLocked(LOCKOUT_EXPIRES_AT)).isFalse();
        assertThat(user.isLocked(LOCKOUT_EXPIRES_AT.plusMillis(1))).isFalse();
    }

    @Test
    @DisplayName("isLocked should be false exactly at and after the lockout expiration instant")
    void isLocked_AtLockoutExpiration_ShouldBeFalse() {
        user.incrementFailedAttempts(1, LOCKOUT, NOW);

        assertThat(user.getLockoutUntil()).isEqualTo(LOCKOUT_EXPIRES_AT);
        assertThat(user.isLocked(LOCKOUT_EXPIRES_AT.minusMillis(1))).isTrue();
        assertThat(user.isLocked(LOCKOUT_EXPIRES_AT)).isFalse();
        assertThat(user.isLocked(LOCKOUT_EXPIRES_AT.plusMillis(1))).isFalse();
    }

    @Test
    @DisplayName("resetFailedAttempts should clear the counter and the lockout")
    void resetFailedAttempts_ShouldClearState() {
        user.incrementFailedAttempts(1, LOCKOUT, NOW);
        assertThat(user.isLocked(NOW)).isTrue();

        user.resetFailedAttempts();

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockoutUntil()).isNull();
        assertThat(user.isLocked(NOW)).isFalse();
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
