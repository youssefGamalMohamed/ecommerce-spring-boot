package com.app.ecommerce.mq.activemq.message_queue_model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailQueueMessage {

    private String greetingMessage;

}
