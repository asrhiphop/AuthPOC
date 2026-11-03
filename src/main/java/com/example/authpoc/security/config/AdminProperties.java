package com.example.authpoc.security.config;

import com.example.authpoc.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin")
public record AdminProperties(
        @NotBlank
        @Size(min = 3, max = 50)
        String username,
        @NotBlank
        @Email
        String email,
        @NotBlank
        @ValidPassword
        String password
) {
}
