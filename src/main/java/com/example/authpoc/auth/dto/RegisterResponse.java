package com.example.authpoc.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record RegisterResponse(

        @Schema(
                description = "Unique identifier of the created user",
                example = "00000000-0000-0000-0000-000000000000"
        )
        UUID id,

        @Schema(
                description = "Username of the created user",
                example = "testuser1"
        )
        String username,

        @Schema(
                description = "Email address of the created user",
                example = "testuser@example.com"
        )
        String email
) {
}