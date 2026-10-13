package com.example.authpoc.exception;

import org.springframework.http.HttpStatus;

public class SelfOperationException extends DomainException {

    public SelfOperationException(String message) {
        super(HttpStatus.FORBIDDEN, "SELF_OPERATION_FORBIDDEN", message, message);
    }
}
