package com.app.ecommerce.category.config.logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;
import java.util.List;

@Component
public class CompositeHttpLogWriter implements HttpLogWriter {

    private final List<HttpLogWriter> writers;

    @Autowired
    public CompositeHttpLogWriter(List<HttpLogWriter> writers) {
        this.writers = writers;
    }

    @Override
    public boolean isActive() {
        return writers.stream().allMatch(HttpLogWriter::isActive);
    }

    @Override
    public void write(Precorrelation precorrelation, String request) throws IOException {
        for (HttpLogWriter writer : writers) {
            writer.write(precorrelation, request);
        }
    }

    @Override
    public void write(Correlation correlation, String response) throws IOException {
        for (HttpLogWriter writer : writers) {
            writer.write(correlation, response);
        }
    }
}
