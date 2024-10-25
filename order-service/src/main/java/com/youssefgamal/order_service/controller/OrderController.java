package com.youssefgamal.order_service.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.youssefgamal.order_service.dtos.CartDto;
import com.youssefgamal.order_service.dtos.OrderDto;
import com.youssefgamal.order_service.entity.Order;
import com.youssefgamal.order_service.integrations.CartServiceIfc;
import com.youssefgamal.order_service.mappers.OrderMapper;
import com.youssefgamal.order_service.service.OrderServiceIfc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class OrderController {

	private final OrderServiceIfc orderServiceIfc;
	private final OrderMapper orderMapper;
	private final CartServiceIfc cartServiceIfc;
	
	@PostMapping("/orders")
	@ResponseStatus(HttpStatus.CREATED)
	public OrderDto createOrder(@RequestBody OrderDto OrderDto) {
		log.info("createOrder({})", OrderDto);
		com.youssefgamal.order_service.entity.Order newCreatedOrder = orderServiceIfc.createOrder(orderMapper.mapToEntity(OrderDto));
		OrderDto newCreatedOrderDto = orderMapper.mapToDto(newCreatedOrder);
		log.info("createOrder({})", newCreatedOrderDto);
		return newCreatedOrderDto;
	}
	
	@GetMapping("/orders/{id}")
	@ResponseStatus(HttpStatus.OK)
	public OrderDto findById(@PathVariable Long id) {
		log.info("findById({})", id);
		Order order = orderServiceIfc.findById(id);
		OrderDto orderDto = orderMapper.mapToDto(order);
		Set<CartDto> cartDtos = order.getCarts()
				.stream()
				.map(cart -> cartServiceIfc.findById(cart.getId()))
				.collect(Collectors.toSet());
		orderDto.setCarts(cartDtos);
		log.info("findById({}), Order: {}", id, orderDto);
		return orderDto; 
	}
}
