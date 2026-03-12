package com.app.ecommerce.dtos;



import com.app.ecommerce.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDto extends BaseDto {

    private Long id;
    private double totalPrice;
	private PaymentType paymentType;
	private DeliveryInfoDto deliveryInfo;
	private CartDto cart;
}
