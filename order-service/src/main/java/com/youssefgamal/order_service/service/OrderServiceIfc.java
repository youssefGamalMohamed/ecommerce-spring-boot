package com.youssefgamal.order_service.service;

import com.youssefgamal.order_service.entity.Order;

public interface OrderServiceIfc {

	Order createOrder(Order order);
	Order findById(Long id);
}
