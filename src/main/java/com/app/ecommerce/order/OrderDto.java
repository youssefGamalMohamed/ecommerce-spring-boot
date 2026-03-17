package com.app.ecommerce.order;

import com.app.ecommerce.cart.CartDto;
import com.app.ecommerce.shared.dto.BaseDto;
import com.app.ecommerce.shared.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

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

    @Schema(description = "Total price of the order", example = "19999", requiredMode = Schema.RequiredMode.REQUIRED)
    private double totalPrice;

    @Schema(description = "Payment type for the order", example = "CREDIT_CARD", requiredMode = Schema.RequiredMode.REQUIRED)
    private PaymentType paymentType;

    @Schema(description = "Delivery information")
    private DeliveryInfoDto deliveryInfo;

    @Schema(description = "Cart associated with the order")
    private CartDto cart;
}
