package com.example.authpoc.exception;

import lombok.Getter;

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
        super(ErrorCode.AUTHENTICATION_FAILED, reason.name());
        this.reason = reason;
        this.username = username;
        this.userId = userId;
    }

}
