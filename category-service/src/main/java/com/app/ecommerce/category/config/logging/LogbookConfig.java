package com.app.ecommerce.category.config.logging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.*;
import org.zalando.logbook.core.DefaultHttpLogWriter;
import org.zalando.logbook.core.DefaultSink;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

@Configuration
public class LogbookConfig {

    @Autowired
    private DatabaseHttpLogWriter databaseHttpLogWriter;

    @Bean
    @Primary
    public CompositeHttpLogWriter compositeHttpLogWriter() {
        return new CompositeHttpLogWriter(List.of(
                new DefaultHttpLogWriter(), // Console logging
                databaseHttpLogWriter // Database logging
        ));
    }

    @Bean
    public Logbook logbookBean() {
        return Logbook.builder()
                .correlationId(new UuidCorrelationId())
                .sink(
                        new DefaultSink(
                                new ConsoleHttpLogFormatter(),
                                compositeHttpLogWriter()
                        )
                )
                .build();
    }
}
