package com.example.authpoc.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ResourceOperationException extends DomainException {

    private final String title;
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;
    private final String internalMessage;

    protected ResourceOperationException(
            HttpStatus status,
            String title,
            String message,
            String resourceName,
            String fieldName,
            Object fieldValue
    ) {
        super(status, title.toUpperCase().replace(' ', '_'), title + ".", message);
        this.title = title;
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.internalMessage = message;
    }

}
