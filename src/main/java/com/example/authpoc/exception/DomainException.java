package com.example.authpoc.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class DomainException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final String publicDetail;

    protected DomainException(HttpStatus status, String code, String publicDetail) {
        this(status, code, publicDetail, code);
    }

    protected DomainException(HttpStatus status, String code, String publicDetail, String internalMessage) {
        super(internalMessage);
        this.status = status;
        this.code = code;
        this.publicDetail = publicDetail;
    }

}
