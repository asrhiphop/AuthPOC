package com.example.authpoc.auth;

import com.example.authpoc.auth.dto.LoginRequest;
import com.example.authpoc.auth.dto.RegisterRequest;
import com.example.authpoc.auth.dto.RegisterResponse;
import com.example.authpoc.auth.token.RefreshTokenService;
import com.example.authpoc.auth.token.dto.TokenResponse;
import com.example.authpoc.exception.InvalidPasswordException;
import com.example.authpoc.security.JwtTokenService;
import com.example.authpoc.user.Role;
import com.example.authpoc.user.UserService;
import com.example.authpoc.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("register should delegate user creation to UserService and return RegisterResponse")
    void register_ShouldCreateUserAndReturnResponse() {
        RegisterRequest request = new RegisterRequest("john_doe", "john@example.com", "SecureP@ss123");
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = new UserResponse(userId, "john_doe", "john@example.com", Role.USER);

        when(userService.createUser(request.username(), request.email(), request.password()))
                .thenReturn(userResponse);

        RegisterResponse response = authService.register(request);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.username()).isEqualTo("john_doe");
        assertThat(response.email()).isEqualTo("john@example.com");

        verify(userService).createUser("john_doe", "john@example.com", "SecureP@ss123");
    }

    @Test
    @DisplayName("login with valid credentials should reset attempts, record login, and issue tokens")
    void login_WithValidCredentials_ShouldReturnTokenResponse() {
        LoginRequest request = new LoginRequest("john_doe", "SecureP@ss123");
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = new UserResponse(userId, "john_doe", "john@example.com", Role.USER);

        when(userService.verifyCredentials("john_doe", "SecureP@ss123")).thenReturn(userResponse);
        when(jwtTokenService.generateToken(userResponse)).thenReturn("access.token.jwt");
        when(refreshTokenService.createRefreshToken(userId)).thenReturn("raw-refresh-token");

        TokenResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access.token.jwt");
        assertThat(response.refreshToken()).isEqualTo("raw-refresh-token");

        verify(userService).resetFailedAttempts("john_doe");
        verify(userService).recordLogin(userId);
        verify(jwtTokenService).generateToken(userResponse);
        verify(refreshTokenService).createRefreshToken(userId);
    }

    @Test
    @DisplayName("login with invalid credentials should record failed attempt and rethrow exception")
    void login_WithInvalidCredentials_ShouldRecordFailedAttemptAndThrow() {
        LoginRequest request = new LoginRequest("john_doe", "WrongPass");

        when(userService.verifyCredentials("john_doe", "WrongPass"))
                .thenThrow(new InvalidPasswordException("john_doe"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("AUTHENTICATION_FAILED");

        verify(userService).recordFailedAttempt("john_doe");

        verify(userService, never()).resetFailedAttempts("john_doe");
        verify(userService, never()).recordLogin(org.mockito.ArgumentMatchers.any());
        verify(jwtTokenService, never()).generateToken(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("login with an unknown username should rethrow and never issue tokens")
    void login_WithUnknownUsername_ShouldThrowAndNotIssueTokens() {
        LoginRequest request = new LoginRequest("ghost", "pw");

        when(userService.verifyCredentials("ghost", "pw"))
                .thenThrow(new com.example.authpoc.exception.UnknownUsernameException("ghost"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(com.example.authpoc.exception.UnknownUsernameException.class);

        verify(jwtTokenService, never()).generateToken(org.mockito.ArgumentMatchers.any());
        verify(refreshTokenService, never()).createRefreshToken(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("login on a locked account should rethrow and never issue tokens")
    void login_OnLockedAccount_ShouldThrowAndNotIssueTokens() {
        LoginRequest request = new LoginRequest("john_doe", "SecureP@ss123");

        when(userService.verifyCredentials("john_doe", "SecureP@ss123"))
                .thenThrow(new com.example.authpoc.exception.AccountLockedException("john_doe"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(com.example.authpoc.exception.AccountLockedException.class);

        verify(userService, never()).resetFailedAttempts("john_doe");
        verify(jwtTokenService, never()).generateToken(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("login should propagate a token-issuing failure after credentials were accepted")
    void login_WhenTokenIssuingFails_ShouldPropagate() {
        LoginRequest request = new LoginRequest("john_doe", "SecureP@ss123");
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = new UserResponse(userId, "john_doe", "john@example.com", Role.USER);

        when(userService.verifyCredentials("john_doe", "SecureP@ss123")).thenReturn(userResponse);
        when(jwtTokenService.generateToken(userResponse)).thenThrow(new IllegalStateException("encoder down"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalStateException.class);

        verify(userService, never()).recordFailedAttempt(org.mockito.ArgumentMatchers.any());
    }
}
