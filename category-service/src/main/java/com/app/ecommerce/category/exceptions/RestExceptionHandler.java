package com.app.ecommerce.category.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {


    @ExceptionHandler({NoSuchElementException.class})
    public ResponseEntity<?> handleNoSuchElement(NoSuchElementException noSuchElementException) {
        log.error("No Such Element Found , Exception : {}", noSuchElementException.getMessage());
        return ResponseEntity.notFound()
                .build();
    }
}
