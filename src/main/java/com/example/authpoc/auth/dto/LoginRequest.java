package com.example.authpoc.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        String username,
        
        @NotBlank
        String password
) {
}
