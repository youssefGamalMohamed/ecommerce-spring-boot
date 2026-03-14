package com.app.ecommerce.shared.exception.type;

public class DuplicatedUniqueColumnValueException extends RuntimeException {

    public DuplicatedUniqueColumnValueException() {
    }

    public DuplicatedUniqueColumnValueException(String message) {
        super(message);
    }
}
