package com.example.authpoc.exception;

public class UnknownUsernameException extends AuthenticationFailureException {

    public UnknownUsernameException(String username) {
        super(AuthenticationFailureReason.UNKNOWN_USERNAME, username);
    }
}
