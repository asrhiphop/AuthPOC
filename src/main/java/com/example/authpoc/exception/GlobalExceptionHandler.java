package com.example.authpoc.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation failed");

        Map<String, List<String>> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        problem.setProperty("errors", errors);
        problem.setProperty("code", "VALIDATION_FAILED");
        return problem;
    }

    @ExceptionHandler(ResourceOperationException.class)
    public ProblemDetail handleResourceOperationException(ResourceOperationException exception) {
        log.info("Resource operation failed: resource={} field={} value={} reason={}",
                exception.getResourceName(), exception.getFieldName(), exception.getFieldValue(),
                exception.getInternalMessage());

        ProblemDetail problem = ProblemDetail.forStatus(exception.getStatus());
        problem.setTitle(exception.getTitle());
        problem.setDetail(exception.getPublicDetail());
        problem.setProperty("code", exception.getCode());

        return problem;
    }

    @ExceptionHandler(AuthenticationFailureException.class)
    public ProblemDetail handleAuthenticationFailure(AuthenticationFailureException exception) {
        log.warn("Authentication failed: reason={} username={}",
                exception.getReason(), exception.getUsername());

        return problem(exception);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ProblemDetail handleInvalidToken(InvalidTokenException exception) {
        log.warn("Refresh token rejected: reason={}", exception.getReason());
        return problem(exception);
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainExceptions(DomainException exception) {
        log.warn("Domain operation failed: code={} detail={}", exception.getCode(), exception.getPublicDetail());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getPublicDetail());
        problem.setTitle(exception.getStatus().getReasonPhrase());
        problem.setProperty("code", exception.getCode());

        return problem;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleSpringAuthentication(AuthenticationException exception) {
        log.warn("Request authentication failed: type={}", exception.getClass().getSimpleName());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Authentication is required.");
        problem.setTitle("Unauthorized");
        problem.setProperty("code", "AUTHENTICATION_REQUIRED");
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException exception) {
        log.warn("Access denied");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, "Access to this resource is forbidden.");
        problem.setTitle("Forbidden");
        problem.setProperty("code", "ACCESS_DENIED");
        return problem;
    }
    private ProblemDetail problem(DomainException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                exception.getStatus(), exception.getPublicDetail());
        problem.setTitle(exception.getStatus().getReasonPhrase());
        problem.setProperty("code", exception.getCode());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception exception) {
        log.error("Unhandled exception caught in global handler: ", exception);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail("An unexpected error occurred. Please contact support.");

        return problem;
    }
}

