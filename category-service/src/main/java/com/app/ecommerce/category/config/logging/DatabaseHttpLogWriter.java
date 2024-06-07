package com.app.ecommerce.category.config.logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Precorrelation;

import java.time.LocalDateTime;

@Component
public class DatabaseHttpLogWriter implements HttpLogWriter {

    @Autowired
    private LogEntryRepo logEntryRepository;

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void write(Precorrelation precorrelation, String request) {
        LogEntry logEntry = new LogEntry();
        logEntry.setRequest(request);
        logEntry.setCorrelationId(precorrelation.getId());
        logEntry.setRequestArrivalTime(LocalDateTime.now());
        logEntryRepository.save(logEntry);
    }

    @Override
    public void write(Correlation correlation, String response) {
        LogEntry logEntry = logEntryRepository.findByCorrelationId(correlation.getId());
        if (logEntry != null) {
            logEntry.setResponse(response);
            logEntry.setRequestFinishTime(LocalDateTime.now());
            logEntryRepository.save(logEntry);
        }
    }
}
