package com.example.authpoc.admin.dto;

import com.example.authpoc.user.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull Role role
) {
}
