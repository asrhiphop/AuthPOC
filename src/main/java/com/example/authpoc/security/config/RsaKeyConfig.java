package com.example.authpoc.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class RsaKeyConfig {

    @Bean
    public JwtEncoder jwtEncoder(RsaKeyProperties properties) {
        return NimbusJwtEncoder
                .withKeyPair(properties.publicKey(), properties.privateKey())
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(RsaKeyProperties properties) {
        return NimbusJwtDecoder
                .withPublicKey(properties.publicKey())
                .build();
    }
}