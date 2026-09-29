package com.example.authpoc.exception;

public class EmailAlreadyExistsException
        extends ResourceAlreadyExistsException {

    public EmailAlreadyExistsException() {
        super("email");
    }
}