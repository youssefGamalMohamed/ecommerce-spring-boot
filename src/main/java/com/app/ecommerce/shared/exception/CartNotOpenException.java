package com.app.ecommerce.shared.exception;

public class CartNotOpenException extends RuntimeException {
    public CartNotOpenException() {
        super("Cart is already checked out and cannot be modified");
    }
}
