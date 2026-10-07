package com.example.authpoc.exception;

import org.springframework.http.HttpStatus;

public abstract class BaseResourceException extends RuntimeException {

    private final HttpStatus status;
    private final String title;
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    protected BaseResourceException(
            HttpStatus status,
            String title,
            String message,
            String resourceName,
            String fieldName,
            Object fieldValue
    ) {
        super(message);
        this.status = status;
        this.title = title;
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}