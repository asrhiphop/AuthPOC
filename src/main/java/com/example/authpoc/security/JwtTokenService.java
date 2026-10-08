package com.example.authpoc.security;

import com.example.authpoc.security.config.JwtProperties;
import com.example.authpoc.user.Permission;
import com.example.authpoc.user.dto.UserResponse;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JwtTokenService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(UserResponse user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.accessTokenExpiration());

        List<String> authorities = user.role().getPermissions().stream()
                .map(Permission::getValue)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.id().toString())
                .claim("role", user.role().name())
                .claim("authorities", authorities)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}