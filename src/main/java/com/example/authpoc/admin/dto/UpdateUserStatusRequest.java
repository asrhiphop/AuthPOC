package com.example.authpoc.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @Schema(
                description = "Enabled user status",
                example = "true"
        )
        @NotNull
        boolean enabled
) {
}
