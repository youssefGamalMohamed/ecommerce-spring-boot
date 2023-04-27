package com.app.ecommerce.exception.type;

import java.util.NoSuchElementException;

public class NameNotFoundException extends NoSuchElementException {
    public NameNotFoundException() {
    }

    public NameNotFoundException(String message) {
        super(message);
    }
}
