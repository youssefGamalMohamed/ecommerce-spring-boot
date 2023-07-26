package com.app.ecommerce.mq.activemq.sender;

import com.app.ecommerce.exception.type.JsonParsingException;
import com.app.ecommerce.mq.activemq.model.EmailQueueMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class DefaultQueueSender {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public boolean sendToQueue(String queueName , Object messageObject) throws JsonParsingException {
        String message = "";
        try {
            message = objectMapper.writeValueAsString(messageObject);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException("Can Not Parse Object = " + messageObject + " to JSON string");
        }
        jmsTemplate.convertAndSend(queueName, message);
        return true;
    }
}
