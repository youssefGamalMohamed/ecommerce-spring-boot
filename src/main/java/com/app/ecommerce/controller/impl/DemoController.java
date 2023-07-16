package com.app.ecommerce.controller.impl;

import com.app.ecommerce.mq.activemq.message_queue_model.EmailQueueMessage;
import com.app.ecommerce.mq.activemq.sender.EmailQueueSender;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class DemoController {

    @Autowired
    private EmailQueueSender emailQueueSender;

    @GetMapping("/demo-api")
    @RolesAllowed({"ADMIN"})
    public String mySendMethod() {
        EmailQueueMessage message = EmailQueueMessage.builder()
                .greetingMessage("Hello World" + LocalDateTime.now())
                .build();
        return emailQueueSender.sendToQueue(message);
    }
}
