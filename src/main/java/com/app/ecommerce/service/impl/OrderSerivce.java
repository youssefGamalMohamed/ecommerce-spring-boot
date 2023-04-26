package com.app.ecommerce.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.ecommerce.entity.Cart;
import com.app.ecommerce.entity.CartItem;
import com.app.ecommerce.entity.Customer;
import com.app.ecommerce.entity.Delivery;
import com.app.ecommerce.entity.Order;
import com.app.ecommerce.entity.Product;
import com.app.ecommerce.enums.Status;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.PostOrderRequestBody;
import com.app.ecommerce.models.response.success.CreateNewOrderResponse;
import com.app.ecommerce.models.response.success.GetOrderByIdResponse;
import com.app.ecommerce.models.response.success.GetOrderStatusById;
import com.app.ecommerce.repository.CartItemRepo;
import com.app.ecommerce.repository.CartRepo;
import com.app.ecommerce.repository.CustomerRepo;
import com.app.ecommerce.repository.OrderRepo;
import com.app.ecommerce.repository.ProductRepo;
import com.app.ecommerce.service.framework.ICartService;
import com.app.ecommerce.service.framework.IOrderService;

@Service
public class OrderSerivce implements IOrderService {

	
	@Autowired
	private CustomerRepo customerRepo;
	
	@Autowired
	private OrderRepo orderRepo;
		
	@Autowired
	private ICartService cartService;
	
	@Override
	public CreateNewOrderResponse createNewOrder(PostOrderRequestBody orderRequestBody) {
		
		Customer customer = customerRepo.findById(orderRequestBody.getCustomerId()).get();
		
		
		Delivery delivery = Delivery.builder()
				.status(Status.NOT_MOVED_OUT_FROM_WAREHOUSE)
				.address(orderRequestBody.getDeliveryAddress())
				.date(orderRequestBody.getDeliveryDate())
				.build();
		
		
		Cart cart = cartService.createNewCart(orderRequestBody.getCart());
				
		Order order = Order.builder()
				.customer(customer)
				.totalPrice(orderRequestBody.getTotalPrice())
				.delivery(delivery)
				.cart(cart)
				.paymentType(orderRequestBody.getPaymentType())
				.createdAt(LocalDateTime.now())
				.build();
				
		cart.setOrder(order);
		orderRepo.save(order);
		
		return CreateNewOrderResponse.builder()
				.id(order.getId())
				.build();
	}

	@Override
	public GetOrderByIdResponse findById(Long orderId) {
		if(!orderRepo.existsById(orderId))
			throw new IdNotFoundException("No Such Order , Id Not Found");
		
		return GetOrderByIdResponse.builder()
				.order(orderRepo.findById(orderId).get())
				.build();
	}

	@Override
	public GetOrderStatusById findOrderStatusById(Long orderId) {
		if(!orderRepo.existsById(orderId))
			throw new IdNotFoundException("No Such Order , Id Not Found");
		
		return GetOrderStatusById.builder()
				.orderStatus(
							orderRepo.findById(orderId)
							.get()
							.getDelivery()
							.getStatus()
						)
				.build();
	}

}
