package com.example.authpoc.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AuthenticationFailureException extends DomainException {

    private final AuthenticationFailureReason reason;
    private final String username;

    protected AuthenticationFailureException(AuthenticationFailureReason reason, String username) {
        super(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "Invalid username or password.");
        this.reason = reason;
        this.username = username;
    }

}
