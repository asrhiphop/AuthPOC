package com.example.authpoc.exception;

public class SelfOperationException extends DomainException {

    public SelfOperationException(String message) {
        super(ErrorCode.SELF_OPERATION_FORBIDDEN, message);
    }
}
