package com.app.ecommerce.mq.activemq.sender;


import com.app.ecommerce.mq.activemq.model.EmailQueueMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmailQueueSender {

    @Value("${activemq.EmailQueue}")
    private String emailQueue;


    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public String sendToQueue(EmailQueueMessage emailQueueMessage) {
        try{
            String message = objectMapper.writeValueAsString(emailQueueMessage);
            jmsTemplate.convertAndSend(emailQueue, message);
            return "OK";
        }catch(JmsException ex){
            ex.printStackTrace();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return "FAILED";
    }
}
