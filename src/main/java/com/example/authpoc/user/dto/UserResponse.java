package com.example.authpoc.user.dto;

import com.example.authpoc.user.Role;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        Role role
) {
}