package com.example.authpoc.exception;

public class AccountLockedException extends AuthenticationFailureException {

    public AccountLockedException(String username) {
        super(AuthenticationFailureReason.ACCOUNT_LOCKED, username);
    }
}
