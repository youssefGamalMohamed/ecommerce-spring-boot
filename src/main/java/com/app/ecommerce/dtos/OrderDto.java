package com.app.ecommerce.dtos;



import com.app.ecommerce.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDto {

    private Long id;
    private double totalPrice;
	private PaymentType paymentType;
	private DeliveryInfoDto deliveryInfo;
	private CartDto cart;
	private UserDto user;
}
