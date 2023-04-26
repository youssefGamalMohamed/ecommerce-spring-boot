package com.app.ecommerce.models.request;



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
public class PostOrderRequestBody {
	
	private Long customerId;
	
	private double totalPrice;
		
	private String deliveryDate;
	
	private String deliveryAddress;
	
	private PaymentType paymentType;
	
	private PostCartRequestBody cart;
	
}
