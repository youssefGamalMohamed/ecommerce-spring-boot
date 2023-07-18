package com.app.ecommerce.mq.activemq.sender;


import com.app.ecommerce.mq.activemq.model.EmailQueueMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class EmailQueueSender {

    @Value("${activemq.EmailQueue}")
    private String emailQueue;


    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public boolean sendToQueue(EmailQueueMessage emailQueueMessage) {
        try{
            String message = objectMapper.writeValueAsString(emailQueueMessage);
            jmsTemplate.convertAndSend(emailQueue, message);
        }catch(JmsException ex){
           ex.printStackTrace();
           log.error("can not send this message = " + emailQueueMessage + " to queue = " + emailQueue);
           return false;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.error("Can not convert this object" + emailQueueMessage + "to json string");
            return false;
        }
        return true;
    }
}
