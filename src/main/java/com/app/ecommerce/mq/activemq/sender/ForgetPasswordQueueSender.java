package com.app.ecommerce.mq.activemq.sender;


import com.app.ecommerce.exception.type.JsonParsingException;
import com.app.ecommerce.mq.activemq.model.ForgetPasswordQueueMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class ForgetPasswordQueueSender {

    @Value("${activemq.ForgetPasswordQueue}")
    private String forgetPasswordQueue;

    @Autowired
    private DefaultQueueSender defaultQueueSender;

    public boolean sendToQueue(ForgetPasswordQueueMessage forgetPasswordQueueMessage) throws JsonParsingException {
        return defaultQueueSender.sendToQueue(forgetPasswordQueue, forgetPasswordQueueMessage);
    }
}
