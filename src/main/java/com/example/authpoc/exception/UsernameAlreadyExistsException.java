package com.example.authpoc.exception;

public class UsernameAlreadyExistsException
        extends ResourceAlreadyExistsException {

    public UsernameAlreadyExistsException() {
        super("username");
    }
}