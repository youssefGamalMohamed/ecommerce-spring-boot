package com.app.ecommerce.order;

import com.app.ecommerce.cart.CartDto;
import com.app.ecommerce.shared.dto.BaseResponse;
import com.app.ecommerce.shared.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Order response")
public class OrderResponse extends BaseResponse {

    @Schema(description = "Unique identifier of the order", accessMode = Schema.AccessMode.READ_ONLY, example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Total price of the order", example = "199.99")
    private BigDecimal totalPrice;

    @Schema(description = "Payment type for the order", example = "CREDIT_CARD")
    private PaymentType paymentType;

    @Schema(description = "Version for optimistic locking", example = "1")
    private Long version;

    @Schema(description = "Delivery information")
    private DeliveryInfoResponse deliveryInfo;

    @Schema(description = "Cart associated with the order")
    private CartDto cart;
}
