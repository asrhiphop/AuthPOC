package com.example.authpoc.exception;

public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
        super("Invalid username or password");
    }
}