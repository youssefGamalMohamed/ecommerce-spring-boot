package com.app.ecommerce.service.impl;

import java.time.LocalDateTime;

import com.app.ecommerce.exception.type.EmailNotFoundException;
import com.app.ecommerce.models.response.endpoints.GetCustomerResponse;
import com.app.ecommerce.mq.activemq.model.InventoryQueueMessage;
import com.app.ecommerce.mq.activemq.sender.InventoryQueueSender;
import com.app.ecommerce.repository.UserRepo;
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
import com.app.ecommerce.models.request.PostOrderRequestBody;
import com.app.ecommerce.models.response.endpoints.CreateNewOrderResponse;
import com.app.ecommerce.models.response.endpoints.GetOrderByIdResponse;
import com.app.ecommerce.models.response.endpoints.GetOrderStatusByIdResponse;
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
	public CreateNewOrderResponse createNewOrder(PostOrderRequestBody orderRequestBody) {

		String customerEmail = ( (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ).getUsername();


		Customer customer = (Customer) userRepo.findByEmail(customerEmail)
				.orElseThrow(() -> new EmailNotFoundException("Can Not Create Order Because Customer Email Not Exist"));
		
		
		DeliveryInfo deliveryInfo = DeliveryInfo.builder()
				.status(Status.NOT_MOVED_OUT_FROM_WAREHOUSE)
				.address(orderRequestBody.getDeliveryAddress())
				.date(orderRequestBody.getDeliveryDate())
				.build();
		
		
		Cart cart = cartService.createNewCart(orderRequestBody.getCart());
				
		Order order = Order.builder()
				.customer(customer)
				.totalPrice(orderRequestBody.getTotalPrice())
				.deliveryInfo(deliveryInfo)
				.cart(cart)
				.paymentType(orderRequestBody.getPaymentType())
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

		return CreateNewOrderResponse.builder()
				.id(order.getId())
				.build();
	}

	@Override
	public GetOrderByIdResponse findById(Long orderId) {
		Order order = orderRepo.findById(orderId).orElseThrow(() -> new IdNotFoundException("No Such Order With This Id , Id Not Found "));

		return GetOrderByIdResponse.builder()
				.id(order.getId())
				.paymentType(order.getPaymentType())
				.totalPrice(order.getTotalPrice())
				.deliveryInfo(order.getDeliveryInfo())
				.cart(order.getCart())
				.customer(
						GetCustomerResponse
								.builder()
								.id(order.getCustomer().getId())
								.firstname(order.getCustomer().getFirstname())
								.lastname(order.getCustomer().getLastname())
								.email(order.getCustomer().getEmail())
								.build()
				)
				.build();
	}

	@Override
	public GetOrderStatusByIdResponse findOrderStatusById(Long orderId) {
		
		return GetOrderStatusByIdResponse.builder()
				.orderStatus(
							orderRepo.findById(orderId)
							.orElseThrow(() -> new IdNotFoundException("No Such Order , Id Not Found"))
							.getDeliveryInfo()
							.getStatus()
						)
				.build();
	}

	@Override
	public void updateOrderStatus(Long orderId, Status orderStatus) {
		Order order = orderRepo.findById(orderId).get();

		order.getDeliveryInfo().setStatus(orderStatus);

		orderRepo.save(order);
	}

}
