package com.example.authpoc.auth.model;

import com.example.authpoc.user.dto.UserResponse;

import java.util.UUID;

public record RegisterResult(
        UUID id,
        String username,
        String email
) {
    public static RegisterResult from(UserResponse user) {
        return new RegisterResult(
                user.id(),
                user.username(),
                user.email()
        );
    }
}
