package com.example.authpoc.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserStatusRequest(
        @Schema(
                description = "Enabled user status",
                example = "true"
        )
        @NotBlank
        boolean enabled
) {
}
