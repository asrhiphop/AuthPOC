package com.example.authpoc.exception;

public class ResourceAlreadyExistsException extends ResourceOperationException {

    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(
                ErrorCode.RESOURCE_ALREADY_EXISTS,
                String.format("%s already exists with %s : '%s'", resourceName, fieldName, fieldValue),
                resourceName,
                fieldName,
                fieldValue
        );
    }
}
