package com.example.authpoc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation failed");

        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(BaseResourceException.class)
    public ProblemDetail handleBaseResourceException(BaseResourceException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(exception.getStatus());
        problem.setTitle(exception.getTitle());
        problem.setProperty("errors", Map.of(exception.getFieldName(), exception.getMessage()));

        return problem;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleAuthenticationFailure(InvalidCredentialsException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Authentication failed");
        problem.setDetail(exception.getMessage());

        return problem;
    }

    @ExceptionHandler(SelfOperationException.class)
    public ProblemDetail handleSelfLockoutException(SelfOperationException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Invalid Operation");
        problem.setDetail(exception.getMessage());

        return problem;
    }
}