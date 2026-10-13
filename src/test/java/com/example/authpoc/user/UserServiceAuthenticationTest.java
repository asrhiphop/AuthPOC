package com.example.authpoc.user;

import com.example.authpoc.exception.AccountLockedException;
import com.example.authpoc.exception.AuthenticationFailureReason;
import com.example.authpoc.exception.AuthenticationFailureException;
import com.example.authpoc.exception.InvalidPasswordException;
import com.example.authpoc.exception.UnknownUsernameException;
import com.example.authpoc.security.config.LockoutProperties;
import com.example.authpoc.user.dto.UserResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceAuthenticationTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                passwordEncoder,
                new LockoutProperties(3, Duration.ofMinutes(15)),
                clock
        );
    }

    @Test
    @DisplayName("unknown username should throw UnknownUsernameException")
    void unknownUsername_ShouldThrow() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.verifyCredentials("ghost", "pw"))
                .isInstanceOf(UnknownUsernameException.class)
                .extracting(ex -> ((AuthenticationFailureException) ex).getReason())
                .isEqualTo(AuthenticationFailureReason.UNKNOWN_USERNAME);
    }

    @Test
    @DisplayName("locked account should throw AccountLockedException without checking the password")
    void lockedAccount_ShouldThrowWithoutCheckingPassword() {
        User locked = org.mockito.Mockito.mock(User.class);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(locked));
        when(locked.isLocked(NOW)).thenReturn(true);

        assertThatThrownBy(() -> userService.verifyCredentials("john", "pw"))
                .isInstanceOf(AccountLockedException.class);

        verify(locked, never()).matchesPassword(any(), any());
    }

    @Test
    @DisplayName("wrong password should throw InvalidPasswordException")
    void wrongPassword_ShouldThrow() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(new User("john", "john@example.com", "hash")));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> userService.verifyCredentials("john", "bad"))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("valid credentials should return the authenticated user")
    void validCredentials_ShouldReturnUser() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(new User("john", "john@example.com", "hash")));
        when(passwordEncoder.matches("pw", "hash")).thenReturn(true);

        UserResponse response = userService.verifyCredentials("john", "pw");

        assertThat(response.username()).isEqualTo("john");
    }
}
