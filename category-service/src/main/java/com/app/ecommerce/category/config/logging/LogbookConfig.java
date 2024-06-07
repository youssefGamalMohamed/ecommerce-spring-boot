package com.app.ecommerce.category.config.logging;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.*;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultHttpLogWriter;
import org.zalando.logbook.core.DefaultSink;
import java.io.IOException;

@Configuration
public class LogbookConfig {
    @Bean
    public Logbook logbookBean() {
        return Logbook.builder()
                .sink(
                        new DefaultSink(
                                new CustomHttpLogFormatter(new DefaultHttpLogFormatter()),
                                new DefaultHttpLogWriter()
                        )
                )
                .build();
    }

    static class CustomHttpLogFormatter implements HttpLogFormatter {
        private final HttpLogFormatter delegate;

        CustomHttpLogFormatter(HttpLogFormatter delegate) {
            this.delegate = delegate;
        }


        @Override
        public String format(Precorrelation precorrelation, HttpRequest request) throws IOException {
            return  "\n" + Strings.repeat("=", 200) + "\n" +
                    "REQUEST >>> \n" + delegate.format(precorrelation, request) + "\n";
        }

        @Override
        public String format(Correlation correlation, HttpResponse response) throws IOException {
            return "\n" + "RESPONSE >>> \n" + delegate.format(correlation, response) +  "\n" + Strings.repeat("=", 200) + "\n" ;

        }
    }

}
