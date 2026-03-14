package com.app.ecommerce.shared.exception.handler;

import com.app.ecommerce.shared.dto.ErrorResponseDto;
import com.app.ecommerce.shared.exception.type.DuplicatedUniqueColumnValueException;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                    @NonNull  HttpHeaders headers,
                                                                    @NonNull HttpStatusCode status,
                                                                    @NonNull WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        log.warn("Validation failed: {}", message);

        ErrorResponseDto errorResponse = ErrorResponseDto.badRequest(
                "Validation failed",
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponseDto> handleInternalServerErrorException(Exception exception, WebRequest request) {
        log.error("Internal Server Error: ", exception);

        ErrorResponseDto errorResponse = ErrorResponseDto.internalError(
                "An unexpected error occurred. Please contact support.",
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = DuplicatedUniqueColumnValueException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicatedUniqueValueException(
            DuplicatedUniqueColumnValueException exception, WebRequest request) {
        log.warn("Conflict error: {}", exception.getMessage());

        ErrorResponseDto errorResponse = ErrorResponseDto.conflict(
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
            IllegalArgumentException exception, WebRequest request) {
        log.warn("Bad request: {}", exception.getMessage());

        ErrorResponseDto errorResponse = ErrorResponseDto.badRequest(
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = NoSuchElementException.class)
    public ResponseEntity<ErrorResponseDto> handleNoSuchElementException(
            NoSuchElementException exception, WebRequest request) {
        log.warn("Resource not found: {}", exception.getMessage());

        ErrorResponseDto errorResponse = ErrorResponseDto.notFound(
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(value = {InvalidDataAccessResourceUsageException.class})
    public ResponseEntity<ErrorResponseDto> handleFailedDatabaseConnectionException(
            InvalidDataAccessResourceUsageException exception, WebRequest request) {
        log.error("The Database Deleted or Table of Database Deleted , Check DB and Tables");

        ErrorResponseDto errorResponse = ErrorResponseDto.serviceUnavailable(
                "Database is unavailable. Please try again later.",
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

}
