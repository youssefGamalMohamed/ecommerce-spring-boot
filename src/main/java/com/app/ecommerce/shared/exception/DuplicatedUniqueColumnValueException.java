package com.app.ecommerce.shared.exception;

public class DuplicatedUniqueColumnValueException extends RuntimeException {

    public DuplicatedUniqueColumnValueException() {
    }

    public DuplicatedUniqueColumnValueException(String message) {
        super(message);
    }
}
