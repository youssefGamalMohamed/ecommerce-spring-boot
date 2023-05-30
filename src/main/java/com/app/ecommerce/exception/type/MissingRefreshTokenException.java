package com.app.ecommerce.exception.type;

import org.springframework.security.core.AuthenticationException;

public class MissingRefreshTokenException extends AuthenticationException {
    public MissingRefreshTokenException(String msg) {
        super(msg);
    }
}
