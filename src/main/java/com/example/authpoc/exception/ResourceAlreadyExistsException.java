package com.example.authpoc.exception;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends ResourceOperationException {

    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(
                HttpStatus.CONFLICT,
                "Resource already exists",
                String.format("%s already exists with %s : '%s'", resourceName, fieldName, fieldValue),
                resourceName,
                fieldName,
                fieldValue
        );
    }
}