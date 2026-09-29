package com.example.authpoc.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 50)
        @Schema(
                description = "Unique username",
                example = "john123"
        )
        String username,

        @NotBlank
        @Email
        @Schema(
                description = "Unique email address",
                example = "john@example.com"
        )
        String email,

        @NotBlank
        @Size(min = 12, max = 128)
        @Schema(
                description = "Account password",
                example = "MySecurePassword123!"
        )
        String password
) {
}
