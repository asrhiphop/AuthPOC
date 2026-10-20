package com.example.authpoc.exception;

import java.util.UUID;

public class AccountLockedException extends AuthenticationFailureException {

    public AccountLockedException(String username) {
        super(AuthenticationFailureReason.ACCOUNT_LOCKED, username);
    }

    public AccountLockedException(String username, UUID userId) {
        super(AuthenticationFailureReason.ACCOUNT_LOCKED, username, userId);
    }
}
