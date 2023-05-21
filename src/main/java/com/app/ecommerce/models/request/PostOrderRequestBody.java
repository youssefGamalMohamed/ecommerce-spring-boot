package com.app.ecommerce.models.request;



import com.app.ecommerce.enums.PaymentType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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


	@NotBlank(message = "Customer User Should Not Be Null or Empty")
	private String customerUserName;

	@Min(2)
	private double totalPrice;

	@NotBlank(message = "Delivery Date Should Not Be Null or Empty")
	private String deliveryDate;


	@NotBlank(message = "Delivery Address Should Not Be Null or Empty")
	private String deliveryAddress;


	private PaymentType paymentType;

	@NotNull(message = "Cart Should Be Attached With Order and Should Not Be Null")
	@Valid
	private PostCartRequestBody cart;
	
}
