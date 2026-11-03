package com.example.authpoc.exception;

public enum InvalidTokenReason {
    TOKEN_NOT_FOUND,
    TOKEN_REUSE_DETECTED,
    TOKEN_EXPIRED,
    USER_DISABLED
}
