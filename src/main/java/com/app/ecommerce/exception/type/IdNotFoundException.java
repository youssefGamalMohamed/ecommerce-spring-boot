package com.app.ecommerce.exception.type;

import java.util.NoSuchElementException;

public class IdNotFoundException extends NoSuchElementException {
    public IdNotFoundException() {
    }

    public IdNotFoundException(String message) {
        super(message);
    }
}
