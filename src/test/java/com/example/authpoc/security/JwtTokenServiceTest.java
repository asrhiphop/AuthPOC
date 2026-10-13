package com.example.authpoc.security;

import com.example.authpoc.security.config.JwtProperties;
import com.example.authpoc.user.Permission;
import com.example.authpoc.user.Role;
import com.example.authpoc.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    private final JwtProperties jwtProperties = new JwtProperties(
            Duration.ofMinutes(15),
            Duration.ofDays(7)
    );
    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private Jwt mockJwt;
    @Captor
    private ArgumentCaptor<JwtEncoderParameters> encoderParametersCaptor;
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService(jwtEncoder, jwtProperties);
    }

    @Test
    @DisplayName("generateToken should construct claims correctly and encode token")
    void generateToken_ShouldBuildCorrectClaimsAndEncode() {
        UUID userId = UUID.randomUUID();
        UserResponse mockUser = new UserResponse(
                userId,
                "john_doe",
                "john@example.com",
                Role.ADMIN
        );

        when(mockJwt.getTokenValue()).thenReturn("mocked.jwt.token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        Instant beforeExecution = Instant.now();

        String resultToken = jwtTokenService.generateToken(mockUser);

        Instant afterExecution = Instant.now();

        assertThat(resultToken).isEqualTo("mocked.jwt.token");

        verify(jwtEncoder).encode(encoderParametersCaptor.capture());
        JwtClaimsSet claims = encoderParametersCaptor.getValue().getClaims();

        assertThat(claims.getClaimAsString("iss")).isEqualTo("self");
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.getClaimAsString("role")).isEqualTo(Role.ADMIN.toString());

        List<String> expectedPermissions = Role.ADMIN.getPermissions().stream()
                .map(Permission::getValue)
                .toList();
        List<String> actualAuthorities = claims.getClaimAsStringList("authorities");
        assertThat(actualAuthorities).containsExactlyElementsOf(expectedPermissions);

        Instant issuedAt = claims.getIssuedAt();
        assertThat(issuedAt).isNotNull();

        assertThat(issuedAt).isBetween(beforeExecution, afterExecution);
        assertThat(claims.getExpiresAt()).isEqualTo(issuedAt.plus(Duration.ofMinutes(15)));
    }
}