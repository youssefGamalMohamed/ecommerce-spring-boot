package com.app.ecommerce.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.ecommerce.controller.framework.IOrderController;
import com.app.ecommerce.dtos.OrderDto;
import com.app.ecommerce.entity.Order;
import com.app.ecommerce.mappers.OrderMapper;
import com.app.ecommerce.service.impl.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.annotation.security.RolesAllowed;

@RestController
public class OrderController implements IOrderController {

	@Autowired
	private OrderService orderService;

	@RolesAllowed({"USER"})
	@PostMapping("/orders")
	@Override
	public ResponseEntity<?> createNewOrder(@RequestBody OrderDto orderDto) throws JsonProcessingException {
		System.out.println(orderDto);
		Order createdOrder = orderService.createNewOrder(OrderMapper.INSTANCE.mapToEntity(orderDto));
		
		return new ResponseEntity<>(
					OrderMapper.INSTANCE.mapToDto(createdOrder),
					HttpStatus.CREATED
				);
	}

	@RolesAllowed({"ADMIN" , "USER"})
	@GetMapping("/orders/{id}")
	@Override
	public ResponseEntity<?> findOrderById(@PathVariable("id") Long orderId) {
		Order order = orderService.findById(orderId);
		
		return new ResponseEntity<> (
					OrderMapper.INSTANCE.mapToDto(order),
					HttpStatus.OK
				);
	}
}
