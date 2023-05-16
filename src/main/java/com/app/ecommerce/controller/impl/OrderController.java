package com.app.ecommerce.controller.impl;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.ecommerce.models.request.PostOrderRequestBody;
import com.app.ecommerce.service.impl.OrderService;

@RestController
public class OrderController {

	@Autowired
	private OrderService orderService;

	@RolesAllowed({"USER"})
	@PostMapping("/orders")
	public ResponseEntity<?> createNewOrder(@RequestBody PostOrderRequestBody orderRequestBody) {
		System.out.println(orderRequestBody);
		return new ResponseEntity<>(
					orderService.createNewOrder(orderRequestBody) ,
					HttpStatus.CREATED
				);
	}

	@RolesAllowed({"ADMIN" , "USER"})
	@GetMapping("/orders/{id}")
	public ResponseEntity<?> findOrderById(@PathVariable("id") Long orderId) {
		
		return new ResponseEntity<> (
					orderService.findById(orderId),
					HttpStatus.OK
				);
	}
}
