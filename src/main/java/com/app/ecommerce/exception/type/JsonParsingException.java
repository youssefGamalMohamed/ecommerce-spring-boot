package com.app.ecommerce.exception.type;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class JsonParsingException extends JsonProcessingException {

    public JsonParsingException(String message) {
        super(message);
    }
}
