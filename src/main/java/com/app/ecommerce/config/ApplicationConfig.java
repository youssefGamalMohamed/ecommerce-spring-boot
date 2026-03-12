package com.app.ecommerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Bean
    ObjectMapper getObjectMapper() {
        return new ObjectMapper()
                            .enable(SerializationFeature.INDENT_OUTPUT) // this for making the json string values in each line
                            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // serialize dates as ISO-8601 strings
                            .registerModule(new JavaTimeModule()); // this for support java 8 date and time if u remove this it wil throw exceptions
    }
}
