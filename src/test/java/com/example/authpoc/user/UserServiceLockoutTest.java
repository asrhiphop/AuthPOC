package com.example.authpoc.user;

import com.example.authpoc.security.config.LockoutProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceLockoutTest {

    private static final LockoutProperties LOCKOUT = new LockoutProperties(3, Duration.ofMinutes(15));
    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private User user;

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, LOCKOUT, clock);
    }

    @Test
    @DisplayName("recordFailedAttempt should apply the configured lockout policy and save the user")
    void recordFailedAttempt_ShouldApplyPolicyAndSave() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        userService.recordFailedAttempt("john");

        verify(user).incrementFailedAttempts(LOCKOUT.maxAttempts(), LOCKOUT.duration(), NOW);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("recordFailedAttempt for an unknown username should do nothing")
    void recordFailedAttempt_UnknownUser_ShouldDoNothing() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        userService.recordFailedAttempt("ghost");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetFailedAttempts should reset and save when the user has failed attempts")
    void resetFailedAttempts_WithFailures_ShouldResetAndSave() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(user.getFailedLoginAttempts()).thenReturn(2);

        userService.resetFailedAttempts("john");

        verify(user).resetFailedAttempts();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("resetFailedAttempts should not write when there are no failed attempts")
    void resetFailedAttempts_NoFailures_ShouldNotSave() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(user.getFailedLoginAttempts()).thenReturn(0);

        userService.resetFailedAttempts("john");

        verify(user, never()).resetFailedAttempts();
        verify(userRepository, never()).save(any());
    }
}
