package com.app.ecommerce.controller.impl;

import com.app.ecommerce.controller.framework.IOrderController;
import com.app.ecommerce.dtos.OrderDto;
import com.app.ecommerce.entity.Order;
import com.app.ecommerce.mappers.OrderMapper;
import com.app.ecommerce.service.impl.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class OrderController implements IOrderController {

	private final OrderService orderService;
	private final OrderMapper orderMapper;

	@PostMapping("/orders")
	@Override
	public ResponseEntity<?> createNewOrder(@RequestBody OrderDto orderDto) throws JsonProcessingException {
		Order order = orderMapper.mapToEntity(orderDto);
		Order createdOrder = orderService.createNewOrder(order);
		
		return new ResponseEntity<>(
					orderMapper.mapToDto(createdOrder),
					HttpStatus.CREATED
				);
	}

	@PutMapping("/orders/{id}")
	@Override
	public ResponseEntity<?> updateOrder(@PathVariable("id") Long orderId, @RequestBody OrderDto orderDto) {
		Order updatedOrder = orderMapper.mapToEntity(orderDto);
		orderService.updateOrder(orderId, updatedOrder);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/orders/{id}")
	@Override
	public ResponseEntity<?> findOrderById(@PathVariable("id") Long orderId) {
		Order order = orderService.findById(orderId);
		
		return new ResponseEntity<> (
					orderMapper.mapToDto(order),
					HttpStatus.OK
				);
	}
}
