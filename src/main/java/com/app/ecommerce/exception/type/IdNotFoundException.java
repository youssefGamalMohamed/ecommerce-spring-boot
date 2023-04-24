package com.app.ecommerce.exception.type;

public class IdNotFoundException extends RuntimeException {
    public IdNotFoundException() {
    }

    public IdNotFoundException(String message) {
        super(message);
    }
}
