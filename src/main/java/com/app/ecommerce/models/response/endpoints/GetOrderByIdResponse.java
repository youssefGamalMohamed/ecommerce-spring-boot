package com.app.ecommerce.models.response.endpoints;

import com.app.ecommerce.entity.Cart;
import com.app.ecommerce.entity.DeliveryInfo;
import com.app.ecommerce.entity.Order;

import com.app.ecommerce.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GetOrderByIdResponse {
	private Long id;
	private PaymentType paymentType;
	private double totalPrice;
	private DeliveryInfo deliveryInfo;
	private Cart cart;
	private GetCustomerResponse customer;
}
