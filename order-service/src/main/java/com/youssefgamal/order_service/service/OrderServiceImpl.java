package com.youssefgamal.order_service.service;

import org.springframework.stereotype.Service;

import com.youssefgamal.order_service.entity.Order;
import com.youssefgamal.order_service.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderServiceIfc {

	private final OrderRepository orderRepository;
	
	@Override
	public Order createOrder(Order order) {
		log.info("createOrder(): " +  order);
		order.getCart().setOrder(order);
		Order createdOrder = orderRepository.save(order);
		log.info("createOrder(): " +  createdOrder);
		return createdOrder;
	}

	@Override
	public Order findById(Long id) {
		log.info("findById({}): ", id);
		Order order =  orderRepository.findById(id)
							.orElseThrow();
		log.info("findById({}): {}", id, order);
		return order;
	}

}
