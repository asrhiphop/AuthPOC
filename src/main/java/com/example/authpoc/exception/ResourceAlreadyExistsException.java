package com.example.authpoc.exception;

public class ResourceAlreadyExistsException extends RuntimeException {

    private final String field;

    public ResourceAlreadyExistsException(String field) {
        super("already exists");
        this.field = field;
    }

    public String getField() {
        return field;
    }
}