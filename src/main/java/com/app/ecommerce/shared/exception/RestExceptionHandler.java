package com.app.ecommerce.shared.exception;

import com.app.ecommerce.shared.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        log.warn("Validation failed: {}", message);

        ErrorResponse errorResponse = ErrorResponse.badRequest(
                "Validation failed",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleInternalServerErrorException(Exception exception, WebRequest request) {
        log.error("Internal Server Error: ", exception);

        ErrorResponse errorResponse = ErrorResponse.internalError(
                "An unexpected error occurred. Please contact support.",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(value = DuplicatedUniqueColumnValueException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatedUniqueValueException(
            DuplicatedUniqueColumnValueException exception, WebRequest request) {
        log.warn("Conflict error: {}", exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.conflict(
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception, WebRequest request) {
        log.warn("Bad request: {}", exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.badRequest(
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(
            NoSuchElementException exception, WebRequest request) {
        log.warn("Resource not found: {}", exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.notFound(
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }


    @ExceptionHandler(value = {InvalidDataAccessResourceUsageException.class})
    public ResponseEntity<ErrorResponse> handleFailedDatabaseConnectionException(
            InvalidDataAccessResourceUsageException exception, WebRequest request) {
        log.error("The Database Deleted or Table of Database Deleted , Check DB and Tables");

        ErrorResponse errorResponse = ErrorResponse.serviceUnavailable(
                "Database is unavailable. Please try again later.",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(value = ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(
            ObjectOptimisticLockingFailureException exception, WebRequest request) {
        log.warn("Optimistic locking failure: {}", exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.conflict(
                "Resource was modified by another user. Please refresh and try again.",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(value = InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateTransitionException(
            InvalidStateTransitionException exception, WebRequest request) {
        log.warn("Invalid state transition: {}", exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.badRequest(
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException exception, WebRequest request) {
        log.warn("Access denied: {}", exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.forbidden(
                "Access denied. You do not have permission to perform this action.",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException exception, WebRequest request) {
        log.warn("Bad credentials: {}", exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.unauthorized(
                "Invalid username or password.",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(value = AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException exception, WebRequest request) {
        log.warn("Authentication error: {}", exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.unauthorized(
                "Authentication failed.",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(value = CartNotOpenException.class)
    public ResponseEntity<ErrorResponse> handleCartNotOpenException(
            CartNotOpenException exception, WebRequest request) {
        log.warn("Cart not open: {}", exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.conflict(
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

}
