package com.example.authpoc.exception;

import org.springframework.http.HttpStatus;

public abstract class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    protected DomainException(ErrorCode errorCode) {
        this(errorCode, errorCode.getCode());
    }

    protected DomainException(ErrorCode errorCode, String internalMessage) {
        super(internalMessage);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }

    public String getCode() {
        return errorCode.getCode();
    }

    public String getPublicDetail() {
        return errorCode.getPublicDetail();
    }

}
