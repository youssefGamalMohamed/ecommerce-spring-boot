package com.app.ecommerce.mq.activemq.listener;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.app.ecommerce.email.model.VerificationRegistrationMessageDetails;
import com.app.ecommerce.email.service.VerificationRegistrationEmailService;
import com.app.ecommerce.exception.type.JsonParsingException;
import com.app.ecommerce.mq.activemq.model.EmailQueueMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import jakarta.mail.MessagingException;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class EmailQueueListener {


    @Value("${activemq.EmailQueue}")
    private String emailQueue;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VerificationRegistrationEmailService verificationRegistrationEmailService;

    @JmsListener(destination = "EmailQueue")
    public void receiveMessage(Message message) throws JMSException, JsonParsingException, MessagingException, UnsupportedEncodingException {
        TextMessage textMessage = (TextMessage) message;
        String payload = textMessage.getText();
        EmailQueueMessage emailQueueMessage = null;
        try {
            emailQueueMessage = objectMapper.readValue(payload , EmailQueueMessage.class);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException("Can not Parse JSON String = " + payload + " to Object of Type " + emailQueueMessage.getClass().getSimpleName());
        }
        log.info("Received From Queue = " + emailQueue + " , Message = \n" + emailQueueMessage + "\n");

        verificationRegistrationEmailService.sendVerificationEmailToRegisteredUser(
                VerificationRegistrationMessageDetails.builder()
                        .email(emailQueueMessage.getEmail())
                        .firstName(emailQueueMessage.getFirstname())
                        .lastName(emailQueueMessage.getLastname())
                        .verificationToken(emailQueueMessage.getVerificationToken())
                        .build()
        );
    }
}
