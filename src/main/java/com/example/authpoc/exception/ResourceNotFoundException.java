package com.example.authpoc.exception;

public class ResourceNotFoundException extends ResourceOperationException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue),
                resourceName,
                fieldName,
                fieldValue
        );
    }
}
