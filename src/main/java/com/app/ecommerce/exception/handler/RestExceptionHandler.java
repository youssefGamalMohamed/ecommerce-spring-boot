package com.app.ecommerce.exception.handler;


import com.app.ecommerce.exception.type.DuplicatedUniqueColumnValueException;
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
        log.warn("Validation failed: {}", ex.getMessage());
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleInternalServerErrorException(Exception exception) {
        log.error("Internal Server Error: ", exception);
    	return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = DuplicatedUniqueColumnValueException.class)
    public ResponseEntity<?> handleDuplicatedUniqueValueException(DuplicatedUniqueColumnValueException exception) {
        log.warn("Conflict error: {}", exception.getMessage());
    	return new ResponseEntity<>(
    				 HttpStatus.CONFLICT
    			);
    }

    @ExceptionHandler(value = NoSuchElementException.class)
    public ResponseEntity<?> handleNoSuchElementException(NoSuchElementException exception) {
        log.warn("Resource not found: {}", exception.getMessage());
    	return new ResponseEntity<>(
    				HttpStatus.NOT_FOUND
    			);
    }


    @ExceptionHandler(value = { InvalidDataAccessResourceUsageException.class })
    public ResponseEntity<?> handleFailedDatabaseConnectionException(InvalidDataAccessResourceUsageException exception) {
        log.error("The Database Deleted or Table of Database Deleted , Check DB and Tables");
        return new ResponseEntity<>(
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

}
