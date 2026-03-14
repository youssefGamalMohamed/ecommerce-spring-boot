package com.app.ecommerce.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;

public interface OrderService {

    Order createNewOrder(Order order) throws JsonProcessingException;

    Order findById(UUID orderId);

    void updateOrder(UUID orderId, Order order);
}
