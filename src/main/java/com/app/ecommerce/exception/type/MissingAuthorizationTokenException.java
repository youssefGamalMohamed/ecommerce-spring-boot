package com.app.ecommerce.exception.type;

public class MissingAuthorizationTokenException extends RuntimeException {

    public MissingAuthorizationTokenException() {
    }

    public MissingAuthorizationTokenException(String message) {
        super(message);
    }
}
