package com.example.authpoc.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problem = problem(ErrorCode.VALIDATION_FAILED);
        Map<String, List<String>> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));
        problem.setProperty("errors", errors);

        return handleExceptionInternal(
                exception,
                problem,
                headers,
                ErrorCode.VALIDATION_FAILED.getStatus(),
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return response(exception, ErrorCode.MALFORMED_REQUEST, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return response(exception, ErrorCode.INVALID_PARAMETER, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return response(exception, ErrorCode.METHOD_NOT_ALLOWED, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return response(exception, ErrorCode.UNSUPPORTED_MEDIA_TYPE, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return response(exception, ErrorCode.MISSING_PARAMETER, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return response(exception, ErrorCode.ENDPOINT_NOT_FOUND, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return response(exception, ErrorCode.ENDPOINT_NOT_FOUND, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return response(exception, ErrorCode.VALIDATION_FAILED, headers, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return handleClientFailure(exception, ErrorCode.RESOURCE_ALREADY_EXISTS);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLockingFailure(OptimisticLockingFailureException exception) {
        return handleClientFailure(exception, ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        ProblemDetail problem = handleClientFailure(exception, ErrorCode.VALIDATION_FAILED);
        Map<String, List<String>> errors = exception.getConstraintViolations()
                .stream()
                .collect(Collectors.groupingBy(
                        violation -> violation.getPropertyPath().toString(),
                        Collectors.mapping(violation -> violation.getMessage(), Collectors.toList())
                ));
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException exception) {
        logClientFailure(exception, exception.getErrorCode());
        return problem(exception.getErrorCode());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleSpringAuthentication(AuthenticationException exception) {
        return handleClientFailure(exception, ErrorCode.AUTHENTICATION_REQUIRED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException exception) {
        return handleClientFailure(exception, ErrorCode.ACCESS_DENIED);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception exception) {
        log.error("Unhandled exception: type={}", exception.getClass().getSimpleName(), exception);
        return problem(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        logRequestFailure(exception, status);
        return super.handleExceptionInternal(exception, body, headers, status, request);
    }

    private ResponseEntity<Object> response(
            Exception exception,
            ErrorCode errorCode,
            HttpHeaders headers,
            WebRequest request
    ) {
        return handleExceptionInternal(
                exception,
                problem(errorCode),
                headers,
                errorCode.getStatus(),
                request
        );
    }

    private ProblemDetail handleClientFailure(Exception exception, ErrorCode errorCode) {
        logClientFailure(exception, errorCode);
        return problem(errorCode);
    }

    private void logClientFailure(Exception exception, ErrorCode errorCode) {
        log.warn("Request failed: code={} exception={}",
                errorCode.getCode(),
                exception.getClass().getSimpleName());
    }

    private void logRequestFailure(Exception exception, HttpStatusCode status) {
        if (status.is5xxServerError()) {
            log.error("Request failed with status {}: type={}",
                    status.value(),
                    exception.getClass().getSimpleName(),
                    exception);
        } else if (status.is4xxClientError()) {
            log.warn("Request failed with status {}: type={}",
                    status.value(),
                    exception.getClass().getSimpleName());
        }
    }

    private ProblemDetail problem(ErrorCode errorCode) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                errorCode.getStatus(),
                errorCode.getPublicDetail()
        );
        problem.setTitle(errorCode.getStatus().getReasonPhrase());
        problem.setProperty("code", errorCode.getCode());
        return problem;
    }
}
