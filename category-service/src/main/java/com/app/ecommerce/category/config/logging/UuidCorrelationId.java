package com.app.ecommerce.category.config.logging;

import org.zalando.logbook.CorrelationId;
import org.zalando.logbook.HttpRequest;

import java.util.UUID;

public class UuidCorrelationId implements CorrelationId {

    @Override
    public String generate(HttpRequest request) {
        return UUID.randomUUID().toString();
    }
}
