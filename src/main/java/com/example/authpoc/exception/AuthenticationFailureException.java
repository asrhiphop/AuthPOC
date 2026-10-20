package com.example.authpoc.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Getter
public abstract class AuthenticationFailureException extends DomainException {

    private final AuthenticationFailureReason reason;
    private final String username;
    private final UUID userId;

    protected AuthenticationFailureException(AuthenticationFailureReason reason, String username) {
        this(reason, username, null);
    }

    protected AuthenticationFailureException(
            AuthenticationFailureReason reason,
            String username,
            UUID userId
    ) {
        super(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "Invalid username or password.");
        this.reason = reason;
        this.username = username;
        this.userId = userId;
    }

}
