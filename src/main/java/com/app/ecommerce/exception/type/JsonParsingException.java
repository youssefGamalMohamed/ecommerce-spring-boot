package com.app.ecommerce.exception.type;

import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonParsingException extends JsonProcessingException {

    public JsonParsingException(String message) {
        super(message);
    }
}
