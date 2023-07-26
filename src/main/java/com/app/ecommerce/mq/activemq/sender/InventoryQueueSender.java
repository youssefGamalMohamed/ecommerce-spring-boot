package com.app.ecommerce.mq.activemq.sender;


import com.app.ecommerce.mq.activemq.model.InventoryQueueMessage;
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
public class InventoryQueueSender {

    @Value("${activemq.InventoryQueue}")
    private String inventoryQueue;

    @Autowired
    private DefaultQueueSender defaultQueueSender;

    public boolean sendToQueue(InventoryQueueMessage inventoryQueueMessage) throws JsonProcessingException,JmsException {
        return defaultQueueSender.sendToQueue(inventoryQueue , inventoryQueueMessage);
    }
}
