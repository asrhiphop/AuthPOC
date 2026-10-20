package com.example.authpoc.exception;

import java.util.UUID;

public class InvalidPasswordException extends AuthenticationFailureException {

    public InvalidPasswordException(String username) {
        super(AuthenticationFailureReason.INVALID_PASSWORD, username);
    }

    public InvalidPasswordException(String username, UUID userId) {
        super(AuthenticationFailureReason.INVALID_PASSWORD, username, userId);
    }
}
