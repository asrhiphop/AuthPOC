package com.example.authpoc.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseResourceException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue),
                resourceName,
                fieldName,
                fieldValue
        );
    }
}