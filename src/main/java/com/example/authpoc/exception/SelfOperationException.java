package com.example.authpoc.exception;

public class SelfOperationException extends RuntimeException {
    public SelfOperationException(String message) {
        super(message);
    }
}
