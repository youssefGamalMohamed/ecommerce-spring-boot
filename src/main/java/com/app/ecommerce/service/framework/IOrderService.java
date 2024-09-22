package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.Order;
import com.app.ecommerce.enums.Status;
import com.fasterxml.jackson.core.JsonProcessingException;


public interface IOrderService {

	Order createNewOrder(Order order) throws JsonProcessingException;
    
    Order findById(Long orderId);
    
    Order findOrderStatusById(Long orderId);

    void updateOrderStatus(Long orderId, Status orderStatus);
}
