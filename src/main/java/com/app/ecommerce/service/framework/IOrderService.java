package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.Order;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface IOrderService {

    Order createNewOrder(Order order) throws JsonProcessingException;

    Order findById(Long orderId);



    void updateOrder(Long orderId, Order order);
}
