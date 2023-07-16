package com.app.ecommerce.mq.activemq.listener;

import com.app.ecommerce.email.model.GreetingNewRegisteredUserMessageDetails;
import com.app.ecommerce.email.service.GreetingNewRegisteredUserEmailService;
import com.app.ecommerce.mq.activemq.model.EmailQueueMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class EmailQueueListener {


    @Value("${activemq.EmailQueue}")
    private String emailQueue;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GreetingNewRegisteredUserEmailService greetingNewRegisteredUserEmailService;

    @JmsListener(destination = "EmailQueue")
    public void receiveMessage(Message message) throws JMSException, JsonProcessingException {
        TextMessage textMessage = (TextMessage) message;
        String payload = textMessage.getText();
        EmailQueueMessage emailQueueMessage = objectMapper.readValue(payload , EmailQueueMessage.class);
        log.info("Received <" + emailQueueMessage + ">");

        greetingNewRegisteredUserEmailService.sendGreetingMessageToNewRegisteredUser(
                GreetingNewRegisteredUserMessageDetails.builder()
                        .email(emailQueueMessage.getEmail())
                        .firstName(emailQueueMessage.getFirstname())
                        .lastName(emailQueueMessage.getLastname())
                        .build()
        );
    }
}
