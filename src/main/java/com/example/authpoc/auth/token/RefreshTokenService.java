package com.example.authpoc.auth.token;

import com.example.authpoc.auth.token.dto.TokenResponse;
import com.example.authpoc.exception.InvalidTokenException;
import com.example.authpoc.security.JwtTokenService;
import com.example.authpoc.security.config.JwtProperties;
import com.example.authpoc.user.User;
import com.example.authpoc.user.UserRepository;
import com.example.authpoc.user.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final TokenRevocationService tokenRevocationService;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            JwtTokenService jwtTokenService,
            JwtProperties jwtProperties,
            TokenRevocationService tokenRevocationService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Transactional
    public String createRefreshToken(UUID userId) {
        return createRefreshTokenInFamily(userId, UUID.randomUUID());
    }

    private String createRefreshTokenInFamily(UUID userId, UUID familyId) {
        User user = userRepository.getReferenceById(userId);
        String tokenValue = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(jwtProperties.refreshTokenExpiration());

        RefreshToken refreshToken = new RefreshToken(user, tokenValue, expiryDate, familyId);
        return refreshTokenRepository.save(refreshToken).getToken();
    }

    @Transactional
    public TokenResponse refresh(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token."));

        // BREACH DETECTED: A revoked token was presented again
        if (refreshToken.isRevoked()) {
            tokenRevocationService.revokeFamily(refreshToken.getFamilyId());
            throw new InvalidTokenException("Security Alert: Token reuse detected. Session terminated.");
        }

        if (refreshToken.isExpired()) {
            tokenRevocationService.deleteExpiredToken(refreshToken.getId());
            throw new InvalidTokenException("Refresh token has expired.");
        }

        User user = refreshToken.getUser();

        if (!user.isEnabled()) {
            tokenRevocationService.revokeFamily(refreshToken.getFamilyId());
            throw new InvalidTokenException("User account is disabled.");
        }

        // Consume current token (enables reuse detection)
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        String newRefreshToken = createRefreshTokenInFamily(user.getId(), refreshToken.getFamilyId());

        UserResponse userResponse = UserResponse.fromEntity(user);
        String newAccessToken = jwtTokenService.generateToken(userResponse);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void revokeToken(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue)
                .ifPresent(token -> tokenRevocationService.revokeFamily(token.getFamilyId()));
    }

    @Transactional
    public void revokeTokensForUser(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
