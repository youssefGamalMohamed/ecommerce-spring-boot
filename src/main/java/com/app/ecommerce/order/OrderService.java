package com.app.ecommerce.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;

public interface OrderService {

    OrderDto createNewOrder(OrderDto orderDto) throws JsonProcessingException;

    OrderDto findById(UUID orderId);

    void updateOrder(UUID orderId, OrderDto orderDto);
}
