package com.app.ecommerce.service.impl;

import com.app.ecommerce.entity.Order;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.mappers.OrderMapper;
import com.app.ecommerce.repository.OrderRepo;
import com.app.ecommerce.service.framework.IOrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class OrderService implements IOrderService {

	private final OrderRepo orderRepo;
	private final OrderMapper orderMapper;


	@Override
	public Order createNewOrder(Order order) throws JsonProcessingException {
		log.info("createNewOrder({})", order);
		Order createdOrder = orderRepo.save(order);
		log.info("order created with id = {}", createdOrder.getId());
		return createdOrder;
	}

	@Override
	public Order findById(Long orderId) {
		log.info("findById({})", orderId);
		if(orderId == null)
			throw new IllegalArgumentException("orderId == null");

		Order order = orderRepo.findById(orderId)
				.orElseThrow(() -> new IdNotFoundException("No Such Order With This Id , Id Not Found with value = " + orderId));
		
		log.info("order found with id = {}", orderId);
		return order;
	}



	@Override
	public void updateOrder(Long orderId, Order updatedOrder) {
		if (orderId == null)
			throw new IllegalArgumentException("Order Id Not Exist to Update");

		Order existingOrder = orderRepo.findById(orderId)
				.orElseThrow(() -> new IdNotFoundException("Order with id " + orderId + " not found"));

		orderMapper.updateFrom(updatedOrder, existingOrder);

		orderRepo.save(existingOrder);
	}

}
