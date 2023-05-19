package com.app.ecommerce.service.impl;

import java.time.LocalDateTime;

import com.app.ecommerce.exception.type.EmailNotFoundException;
import com.app.ecommerce.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.ecommerce.entity.Cart;
import com.app.ecommerce.entity.Customer;
import com.app.ecommerce.entity.Delivery;
import com.app.ecommerce.entity.Order;
import com.app.ecommerce.enums.Status;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.PostOrderRequestBody;
import com.app.ecommerce.models.response.endpoints.CreateNewOrderResponse;
import com.app.ecommerce.models.response.endpoints.GetOrderByIdResponse;
import com.app.ecommerce.models.response.endpoints.GetOrderStatusById;
import com.app.ecommerce.repository.OrderRepo;
import com.app.ecommerce.service.framework.ICartService;
import com.app.ecommerce.service.framework.IOrderService;

@Service
public class OrderService implements IOrderService {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private OrderRepo orderRepo;
		
	@Autowired
	private ICartService cartService;
	
	@Override
	public CreateNewOrderResponse createNewOrder(PostOrderRequestBody orderRequestBody) {
		
		Customer customer = (Customer) userRepo.findByEmail(orderRequestBody.getCustomerUserName())
				.orElseThrow(() -> new EmailNotFoundException("Can Not Create Order Because Customer Email Not Exist"));
		
		
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
		
		return GetOrderByIdResponse.builder()
				.order(
						orderRepo.findById(orderId).orElseThrow(() -> new IdNotFoundException("No Such Order With This Id , Id Not Found "))
				)
				.build();
	}

	@Override
	public GetOrderStatusById findOrderStatusById(Long orderId) {
		
		return GetOrderStatusById.builder()
				.orderStatus(
							orderRepo.findById(orderId)
							.orElseThrow(() -> new IdNotFoundException("No Such Order , Id Not Found"))
							.getDelivery()
							.getStatus()
						)
				.build();
	}

}
