package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;

public interface IOrderService {

    Order createNewOrder(Order order) throws JsonProcessingException;

    Order findById(UUID orderId);



    void updateOrder(UUID orderId, Order order);
}
