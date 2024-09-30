package com.app.ecommerce.logging;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;


@Component
@Log4j2
public class LoggingUtils {

    public void logSeparator(){
        log.info("=".repeat(200));
    }
}
