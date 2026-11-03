package com.example.authpoc.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void inputMethodValidation_ShouldReturn400() {
        MethodValidationResult validationResult = mock(MethodValidationResult.class);
        when(validationResult.isForReturnValue()).thenReturn(false);
        HandlerMethodValidationException exception = new HandlerMethodValidationException(validationResult);

        ResponseEntity<Object> response = handler.handleHandlerMethodValidationException(
                exception,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                mock(WebRequest.class)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(problem(response).getProperties().get("code"))
                .isEqualTo(ErrorCode.VALIDATION_FAILED.getCode());
    }

    @Test
    void returnValueMethodValidation_ShouldReturn500() {
        MethodValidationResult validationResult = mock(MethodValidationResult.class);
        when(validationResult.isForReturnValue()).thenReturn(true);
        HandlerMethodValidationException exception = new HandlerMethodValidationException(validationResult);

        ResponseEntity<Object> response = handler.handleHandlerMethodValidationException(
                exception,
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                mock(WebRequest.class)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(problem(response).getProperties().get("code"))
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
    }

    @Test
    void constraintViolationOutsideMvc_ShouldReturnSafe500() {
        ConstraintViolationException exception = new ConstraintViolationException(
                "internal validation detail",
                Set.of()
        );

        ProblemDetail problem = handler.handleConstraintViolation(exception);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getProperties().get("code"))
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        assertThat(problem.getDetail()).doesNotContain("internal validation detail");
    }

    private ProblemDetail problem(ResponseEntity<Object> response) {
        return (ProblemDetail) response.getBody();
    }
}
