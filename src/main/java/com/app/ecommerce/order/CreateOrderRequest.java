package com.app.ecommerce.order;

import com.app.ecommerce.shared.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request to create a new order")
public class CreateOrderRequest {

    @NotNull(message = "Payment type is required")
    @Schema(description = "Payment type for the order", example = "CREDIT_CARD", requiredMode = Schema.RequiredMode.REQUIRED)
    private PaymentType paymentType;

    @NotNull(message = "Cart ID is required")
    @Schema(description = "ID of the cart to place order from", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID cartId;
}
