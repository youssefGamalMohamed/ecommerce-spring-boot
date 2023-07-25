package com.app.ecommerce.mq.activemq.model;

import com.app.ecommerce.entity.Order;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryQueueMessage {

    private Order order;
}
