package com.example.authpoc.auth.model;

import java.util.UUID;

public record RegisterResult(
        UUID id,
        String username,
        String email
) {
}
