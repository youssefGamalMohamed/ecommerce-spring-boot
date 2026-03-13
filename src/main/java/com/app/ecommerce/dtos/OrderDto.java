package com.app.ecommerce.dtos;


import com.app.ecommerce.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
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
@Schema(description = "Order data transfer object")
public class OrderDto extends BaseDto {

    @Schema(description = "Unique identifier of the order", accessMode = Schema.AccessMode.READ_ONLY, example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Total price of the order", example = "199.99", required = true)
    private double totalPrice;

    @Schema(description = "Payment type for the order", example = "CREDIT_CARD", required = true)
	private PaymentType paymentType;

    @Schema(description = "Delivery information")
	private DeliveryInfoDto deliveryInfo;

    @Schema(description = "Cart associated with the order")
	private CartDto cart;
}
