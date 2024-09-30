package com.youssefgamal.productservice.exception.handler;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.youssefgamal.productservice.exception.type.DuplicatedUniqueColumnValueException;

import lombok.extern.log4j.Log4j2;

@RestControllerAdvice
@Log4j2
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {


        // get all attributes names that failed in validation
        Set<String> failedAttributesNamesInValidation = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField())
                .collect(Collectors.toSet());


        // create an empty map
        Map<String , List<String>> errorsAndCauses = new HashMap<>();

        // fill map with Key = attribute name that failed in validation , Value = List of causes that this attribute failed in
        failedAttributesNamesInValidation.forEach(error -> {
            errorsAndCauses.put(error , new ArrayList<>());
        });


        // add each validation cause for each attribute in the validation list
        ex.getBindingResult().getFieldErrors()
                .forEach(fieldError -> {
                    errorsAndCauses.get(fieldError.getField()).add(fieldError.getDefaultMessage());
                });


        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleInternalServerErrorException(Exception exception) {
        exception.printStackTrace();
        log.error("ERROR {}",exception.getMessage());
    	return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = DuplicatedUniqueColumnValueException.class)
    public ResponseEntity<?> handleDuplicatedUniqueValueException(DuplicatedUniqueColumnValueException exception) {
    	return new ResponseEntity<>(
    				 HttpStatus.CONFLICT
    			);
    }

    @ExceptionHandler(value = NoSuchElementException.class)
    public ResponseEntity<?> handleNoSuchElementException(NoSuchElementException exception) {
    	return new ResponseEntity<>(
    				HttpStatus.NOT_FOUND
    			);
    }

}
