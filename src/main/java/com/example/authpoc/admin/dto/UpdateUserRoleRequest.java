package com.example.authpoc.admin.dto;

import com.example.authpoc.user.Role;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRoleRequest(
        @NotBlank Role role
) {
}
