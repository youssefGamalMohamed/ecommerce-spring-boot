package com.app.ecommerce.exception.type;

public class DuplicatedUniqueColumnValueException extends RuntimeException {
	
	public DuplicatedUniqueColumnValueException() {}
	
	public DuplicatedUniqueColumnValueException(String message) {
		super(message);
	}
}
