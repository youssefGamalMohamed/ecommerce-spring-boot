package com.app.ecommerce.exception.type;

import java.util.NoSuchElementException;

public class EmailNotFoundException extends NoSuchElementException {
    public EmailNotFoundException() {
    }

    public EmailNotFoundException(String message) {
        super(message);
    }
}
