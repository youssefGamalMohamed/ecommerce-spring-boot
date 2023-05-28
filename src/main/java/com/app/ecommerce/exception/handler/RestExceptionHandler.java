package com.app.ecommerce.exception.handler;


import com.app.ecommerce.exception.type.DuplicatedUniqueColumnValueException;
import com.app.ecommerce.models.response.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
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


        return new ResponseEntity<>(
                BadRequestResponse.builder()
                        .message("Validation Failed")
                        .failed_validation_attributes(errorsAndCauses)
                        .build()
                , HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleInternalServerErrorException(Exception exception) {
    	return new ResponseEntity<>(
    				InternalServerResponse.builder()
    				.message(exception.getMessage())
    				.build()
    				 , HttpStatus.INTERNAL_SERVER_ERROR

    			);
    }

    @ExceptionHandler(value = DuplicatedUniqueColumnValueException.class)
    public ResponseEntity<?> handleDuplicatedUniqueValueException(DuplicatedUniqueColumnValueException exception) {
    	return new ResponseEntity<>(
    				ConflictResponse.builder()
    				.message(exception.getMessage())
    				.build()
    				 , HttpStatus.CONFLICT
    			
    			);
    }

    @ExceptionHandler(value = NoSuchElementException.class)
    public ResponseEntity<?> handleNoSuchElementException(NoSuchElementException exception) {
    	return new ResponseEntity<>(
    				NotFoundResponse.builder()
    				.message(exception.getMessage())
    				.build()
    				 , HttpStatus.NOT_FOUND
    			
    			);
    }



    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException exception) {
        return new ResponseEntity<>(null , HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationExceptionException(AuthenticationException exception) {
        return new ResponseEntity<>(null , HttpStatus.UNAUTHORIZED);
    }
}
