package com.app.ecommerce.order;

import com.app.ecommerce.shared.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request to create a new order. The cart is automatically derived from the authenticated user's current open cart.")
public class CreateOrderRequest {

    @NotNull(message = "Payment type is required")
    @Schema(description = "Payment type for the order", example = "CREDIT_CARD", requiredMode = Schema.RequiredMode.REQUIRED)
    private PaymentType paymentType;
}
