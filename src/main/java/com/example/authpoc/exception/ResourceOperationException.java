package com.example.authpoc.exception;

import lombok.Getter;

@Getter
public abstract class ResourceOperationException extends DomainException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    protected ResourceOperationException(
            ErrorCode errorCode,
            String message,
            String resourceName,
            String fieldName,
            Object fieldValue
    ) {
        super(errorCode, message);
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

}
