package com.example.authpoc.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.lockout")
public record LockoutProperties(
        int maxAttempts,
        Duration duration
) {
}