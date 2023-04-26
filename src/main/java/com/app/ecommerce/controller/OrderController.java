package com.app.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.ecommerce.models.request.PostOrderRequestBody;
import com.app.ecommerce.models.response.success.GetOrderByIdResponse;
import com.app.ecommerce.service.impl.OrderSerivce;

@RestController
public class OrderController {

	@Autowired
	private OrderSerivce orderSerivce;
	
	@PostMapping("/orders")
	public ResponseEntity<?> createNewOrder(@RequestBody PostOrderRequestBody orderRequestBody) {
		System.out.println(orderRequestBody);
		return new ResponseEntity<>(
					orderSerivce.createNewOrder(orderRequestBody) , 
					HttpStatus.CREATED
				);
	}
	
	@GetMapping("/orders/{id}")
	public ResponseEntity<?> findOrderById(@PathVariable("id") Long orderId) {
		
		return new ResponseEntity<> (
					orderSerivce.findById(orderId),
					HttpStatus.OK
				);
	}
}
