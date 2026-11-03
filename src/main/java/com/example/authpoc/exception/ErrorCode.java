package com.example.authpoc.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND",
            "Resource not found."
    ),
    RESOURCE_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "RESOURCE_ALREADY_EXISTS",
            "Resource already exists."
    ),
    AUTHENTICATION_FAILED(
            HttpStatus.UNAUTHORIZED,
            "AUTHENTICATION_FAILED",
            "Invalid username or password."
    ),
    TOKEN_INVALID(
            HttpStatus.UNAUTHORIZED,
            "TOKEN_INVALID",
            "Invalid refresh token."
    ),
    SELF_OPERATION_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "SELF_OPERATION_FORBIDDEN",
            "This operation is not permitted."
    ),
    AUTHENTICATION_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTHENTICATION_REQUIRED",
            "Authentication is required."
    ),
    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "ACCESS_DENIED",
            "Access to this resource is forbidden."
    ),
    VALIDATION_FAILED(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_FAILED",
            "Validation failed."
    ),
    MALFORMED_REQUEST(
            HttpStatus.BAD_REQUEST,
            "MALFORMED_REQUEST",
            "The request body is invalid."
    ),
    INVALID_PARAMETER(
            HttpStatus.BAD_REQUEST,
            "INVALID_PARAMETER",
            "One or more request parameters are invalid."
    ),
    METHOD_NOT_ALLOWED(
            HttpStatus.METHOD_NOT_ALLOWED,
            "METHOD_NOT_ALLOWED",
            "The requested method is not supported."
    ),
    UNSUPPORTED_MEDIA_TYPE(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "UNSUPPORTED_MEDIA_TYPE",
            "The request media type is not supported."
    ),
    MISSING_PARAMETER(
            HttpStatus.BAD_REQUEST,
            "MISSING_PARAMETER",
            "A required request parameter is missing."
    ),
    ENDPOINT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "ENDPOINT_NOT_FOUND",
            "The requested endpoint was not found."
    ),
    OPTIMISTIC_LOCK_CONFLICT(
            HttpStatus.CONFLICT,
            "OPTIMISTIC_LOCK_CONFLICT",
            "The resource was modified by another request."
    ),
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. Please contact support."
    );

    private final HttpStatus status;
    private final String code;
    private final String publicDetail;

    ErrorCode(HttpStatus status, String code, String publicDetail) {
        this.status = status;
        this.code = code;
        this.publicDetail = publicDetail;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getPublicDetail() {
        return publicDetail;
    }
}
