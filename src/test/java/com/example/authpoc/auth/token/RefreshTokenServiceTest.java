package com.example.authpoc.auth.token;

import com.example.authpoc.auth.token.dto.TokenResponse;
import com.example.authpoc.exception.InvalidTokenException;
import com.example.authpoc.security.JwtTokenService;
import com.example.authpoc.security.config.JwtProperties;
import com.example.authpoc.user.User;
import com.example.authpoc.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    private final JwtProperties jwtProperties = new JwtProperties(Duration.ofSeconds(30), Duration.ofDays(7));

    private RefreshTokenService service;

    private User user;
    private UUID familyId;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(refreshTokenRepository, userRepository, jwtTokenService, jwtProperties);
        user = new User("john", "john@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        familyId = UUID.randomUUID();
    }

    private RefreshToken token(Instant expiry) {
        return new RefreshToken(user, "raw", expiry, familyId);
    }

    @Test
    @DisplayName("refresh with an unknown token should throw InvalidTokenException")
    void refresh_UnknownToken_ShouldThrow() {
        when(refreshTokenRepository.findByToken("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh("nope"))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenRepository, never()).deleteAllByFamilyId(any());
    }

    @Test
    @DisplayName("refresh with a reused (revoked) token should delete the whole token family and throw")
    void refresh_ReusedToken_ShouldDeleteFamilyAndThrow() {
        RefreshToken revoked = token(Instant.now().plusSeconds(60));
        revoked.revoke();
        when(refreshTokenRepository.findByToken("raw")).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> service.refresh("raw"))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenRepository).deleteAllByFamilyId(familyId);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("refresh with an expired token should delete it and throw")
    void refresh_ExpiredToken_ShouldDeleteAndThrow() {
        RefreshToken expired = token(Instant.now().minusSeconds(60));
        when(refreshTokenRepository.findByToken("raw")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.refresh("raw"))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenRepository).delete(expired);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("refresh for a disabled user should delete the token family and throw")
    void refresh_DisabledUser_ShouldDeleteFamilyAndThrow() {
        user.setEnabled(false);
        RefreshToken valid = token(Instant.now().plusSeconds(60));
        when(refreshTokenRepository.findByToken("raw")).thenReturn(Optional.of(valid));

        assertThatThrownBy(() -> service.refresh("raw"))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenRepository).deleteAllByFamilyId(familyId);
        verify(jwtTokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("refresh with a valid token should rotate it within the same family and issue new tokens")
    void refresh_ValidToken_ShouldRotateWithinFamily() {
        RefreshToken valid = token(Instant.now().plusSeconds(60));
        when(refreshTokenRepository.findByToken("raw")).thenReturn(Optional.of(valid));
        when(userRepository.getReferenceById(user.getId())).thenReturn(user);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenService.generateToken(any())).thenReturn("new.access.jwt");

        TokenResponse response = service.refresh("raw");

        assertThat(valid.isRevoked()).isTrue();
        assertThat(response.accessToken()).isEqualTo("new.access.jwt");
        assertThat(response.refreshToken()).isNotBlank().isNotEqualTo("raw");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        RefreshToken rotated = captor.getAllValues().get(1);
        assertThat(rotated.getFamilyId()).isEqualTo(familyId);
        assertThat(rotated.isRevoked()).isFalse();
    }

    @Test
    @DisplayName("revokeToken with an unknown token should be a silent no-op")
    void revokeToken_Unknown_ShouldDoNothing() {
        when(refreshTokenRepository.findByToken("nope")).thenReturn(Optional.empty());

        service.revokeToken("nope");

        verify(refreshTokenRepository, never()).deleteAllByFamilyId(any());
    }

    @Test
    @DisplayName("revokeToken should delete the whole family of a known token")
    void revokeToken_Known_ShouldDeleteFamily() {
        when(refreshTokenRepository.findByToken("raw")).thenReturn(Optional.of(token(Instant.now().plusSeconds(60))));

        service.revokeToken("raw");

        verify(refreshTokenRepository).deleteAllByFamilyId(familyId);
    }
}
