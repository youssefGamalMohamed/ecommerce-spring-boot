package com.app.ecommerce.category.config.logging;

import org.zalando.logbook.*;
import org.zalando.logbook.json.JsonHttpLogFormatter;

import java.io.IOException;


public class ConsoleHttpLogFormatter implements HttpLogFormatter {

    private final HttpLogFormatter delegate;

    public ConsoleHttpLogFormatter() {
        this(new JsonHttpLogFormatter());
    }

    public ConsoleHttpLogFormatter(HttpLogFormatter delegate) {
        this.delegate = delegate;
    }

    @Override
    public String format(Precorrelation precorrelation, HttpRequest request) throws IOException {
        String request_json_string = delegate.format(precorrelation, request);
        return request_json_string;
    }

    @Override
    public String format(Correlation correlation, HttpResponse response) throws IOException {
        String respnse_json_string = delegate.format(correlation, response);
        return respnse_json_string;
    }

}
