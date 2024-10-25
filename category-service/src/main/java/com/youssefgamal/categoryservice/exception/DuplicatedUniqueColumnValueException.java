package com.youssefgamal.categoryservice.exception;

public class DuplicatedUniqueColumnValueException extends RuntimeException {
	
	public DuplicatedUniqueColumnValueException() {}
	
	public DuplicatedUniqueColumnValueException(String message) {
		super(message);
	}
}
