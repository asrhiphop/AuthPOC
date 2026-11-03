package com.example.authpoc.exception;

import lombok.Getter;

@Getter
public class InvalidTokenException extends DomainException {

    private final InvalidTokenReason reason;

    public InvalidTokenException(InvalidTokenReason reason) {
        super(ErrorCode.TOKEN_INVALID, reason.name());
        this.reason = reason;
    }

}
