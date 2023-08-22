package com.app.ecommerce.mq.activemq.listener;

import com.app.ecommerce.email.model.ResetPasswordMessageDetails;
import com.app.ecommerce.email.service.ResetPasswordEmailService;
import com.app.ecommerce.exception.type.JsonParsingException;
import com.app.ecommerce.mq.activemq.model.ForgetPasswordQueueMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import jakarta.mail.MessagingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
@Log4j2
public class ForgetPasswordQueueListener {


    @Value("${activemq.ForgetPasswordQueue}")
    private String forgetPasswordQueue;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private ResetPasswordEmailService resetPasswordEmailService;

    @JmsListener(destination = "ForgetPasswordQueue")
    public void receiveMessage(Message message) throws JMSException, JsonParsingException, MessagingException, UnsupportedEncodingException {
        TextMessage textMessage = (TextMessage) message;
        String payload = textMessage.getText();
        ForgetPasswordQueueMessage forgetPasswordQueueMessage = null;
        try {
            forgetPasswordQueueMessage = objectMapper.readValue(payload , ForgetPasswordQueueMessage.class);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException("Can not Parse JSON String = " + payload + " to Object of Type " + forgetPasswordQueueMessage.getClass().getSimpleName());
        }
        log.info("Received From Queue = " + forgetPasswordQueue + " , Message = \n" + forgetPasswordQueueMessage + "\n");

        resetPasswordEmailService.sendResetPasswordEmailToUser(
                ResetPasswordMessageDetails.builder()
                        .firstName(forgetPasswordQueueMessage.getFirstname())
                        .lastName(forgetPasswordQueueMessage.getLastname())
                        .email(forgetPasswordQueueMessage.getEmail())
                        .forgetPasswordToken(forgetPasswordQueueMessage.getForgetPasswordToken())
                        .build()
        );
    }
}
