package com.example.authpoc.exception;

public class InvalidPasswordException extends AuthenticationFailureException {

    public InvalidPasswordException(String username) {
        super(AuthenticationFailureReason.INVALID_PASSWORD, username);
    }
}
