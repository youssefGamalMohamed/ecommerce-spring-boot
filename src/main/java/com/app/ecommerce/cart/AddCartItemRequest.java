package com.app.ecommerce.cart;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request to add a product to the cart")
public class AddCartItemRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "ID of the product to add", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity to add (must be >= 1)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private int quantity;
}
