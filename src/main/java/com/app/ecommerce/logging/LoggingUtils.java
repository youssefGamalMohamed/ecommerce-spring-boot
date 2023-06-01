package com.app.ecommerce.logging;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;


@Component
@Log4j2
public class LoggingUtils {

    public void logSeparator(){
        log.info("=".repeat(200));
    }
}
