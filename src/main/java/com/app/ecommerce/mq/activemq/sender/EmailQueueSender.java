package com.app.ecommerce.mq.activemq.sender;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.app.ecommerce.exception.type.JsonParsingException;
import com.app.ecommerce.mq.activemq.model.EmailQueueMessage;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class EmailQueueSender {

    @Value("${activemq.EmailQueue}")
    private String emailQueue;

    @Autowired
    private DefaultQueueSender defaultQueueSender;

    public boolean sendToQueue(EmailQueueMessage emailQueueMessage) throws JsonParsingException {
        return defaultQueueSender.sendToQueue(emailQueue , emailQueueMessage);
    }
}
