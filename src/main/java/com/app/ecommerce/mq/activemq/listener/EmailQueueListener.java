package com.app.ecommerce.mq.activemq.listener;

import com.app.ecommerce.mq.activemq.message_queue_model.EmailQueueMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class EmailQueueListener {


    @Value("${activemq.EmailQueue}")
    private String emailQueue;

    @Autowired
    private ObjectMapper objectMapper;

    @JmsListener(destination = "EmailQueue")
    public void receiveMessage(Message message) throws JMSException {
        TextMessage textMessage = (TextMessage) message;
        String payload = textMessage.getText();
        EmailQueueMessage emailQueueMessage = objectMapper.convertValue(payload , EmailQueueMessage.class);
        System.out.println("Received <" + emailQueueMessage.getGreetingMessage() + ">");
    }
}
