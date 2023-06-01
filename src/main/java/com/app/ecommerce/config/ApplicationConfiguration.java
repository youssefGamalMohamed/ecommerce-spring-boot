package com.app.ecommerce.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {


    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper()
                            .enable(SerializationFeature.INDENT_OUTPUT) // this for making the json string values in each line
                            .registerModule(new JavaTimeModule()); // this for support java 8 date and time if u remove this it wil throw exceptions
    }
}
