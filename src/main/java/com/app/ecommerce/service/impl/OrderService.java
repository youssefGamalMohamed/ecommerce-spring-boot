package com.app.ecommerce.service.impl;

import java.time.LocalDateTime;

import com.app.ecommerce.exception.type.EmailNotFoundException;
import com.app.ecommerce.mq.activemq.model.InventoryQueueMessage;
import com.app.ecommerce.mq.activemq.sender.InventoryQueueSender;
import com.app.ecommerce.repository.UserRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.app.ecommerce.entity.Cart;
import com.app.ecommerce.entity.Customer;
import com.app.ecommerce.entity.DeliveryInfo;
import com.app.ecommerce.entity.Order;
import com.app.ecommerce.enums.Status;
import com.app.ecommerce.exception.type.IdNotFoundException;
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

	@Autowired
	private InventoryQueueSender inventoryQueueSender;

	@Override
	public Order createNewOrder(Order newOrderInfo) throws JsonProcessingException {

		String customerEmail = ( (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ).getUsername();


		Customer customer = (Customer) userRepo.findByEmail(customerEmail)
				.orElseThrow(() -> new EmailNotFoundException("Can Not Create Order Because Customer Email Not Exist"));
		
		
		DeliveryInfo deliveryInfo = DeliveryInfo.builder()
				.status(Status.NOT_MOVED_OUT_FROM_WAREHOUSE)
				.address(newOrderInfo.getDeliveryInfo().getAddress())
				.date(newOrderInfo.getDeliveryInfo().getDate())
				.build();
		
		
		Cart cart = cartService.createNewCart(newOrderInfo.getCart());
				
		Order order = Order.builder()
				.customer(customer)
				.totalPrice(newOrderInfo.getTotalPrice())
				.deliveryInfo(deliveryInfo)
				.cart(cart)
				.paymentType(newOrderInfo.getPaymentType())
				.createdAt(LocalDateTime.now())
				.build();
				
		cart.setOrder(order);
		orderRepo.save(order);

		// send to order queue for process it from inventory
		// then inventory will reply to me with the response for this order in other queue named NewOrderResponseQueueSender
		inventoryQueueSender.sendToQueue(
				InventoryQueueMessage.builder()
						.order(order)
						.build()
		);

		return order;
	}

	@Override
	public Order findById(Long orderId) {
		Order order = orderRepo.findById(orderId).orElseThrow(() -> new IdNotFoundException("No Such Order With This Id , Id Not Found "));
		return order;
	}

	@Override
	public Order findOrderStatusById(Long orderId) {
		
		return orderRepo.findById(orderId)
							.orElseThrow(() -> new IdNotFoundException("No Such Order , Id Not Found"));
				
	}

	@Override
	public void updateOrderStatus(Long orderId, Status orderStatus) {
		Order order = orderRepo.findById(orderId).get();

		order.getDeliveryInfo().setStatus(orderStatus);

		orderRepo.save(order);
	}

}
