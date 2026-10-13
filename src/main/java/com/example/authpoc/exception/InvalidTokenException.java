package com.example.authpoc.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidTokenException extends DomainException {

    private final String reason;

    public InvalidTokenException(String reason) {
        super(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "Invalid refresh token.");
        this.reason = reason;
    }

}
