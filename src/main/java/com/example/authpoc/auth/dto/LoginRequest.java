package com.example.authpoc.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(
                description = "Username of the account",
                example = "testuser1"
        )
        @NotBlank
        String username,

        @Schema(
                description = "Account password",
                example = "MySecurePassword123!"
        )
        @NotBlank
        String password
) {
}
