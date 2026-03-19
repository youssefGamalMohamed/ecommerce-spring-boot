package com.app.ecommerce.cart;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request to update a cart item's quantity. Sending 0 removes the item.")
public class UpdateCartItemQuantityRequest {

    @Min(value = 0, message = "Quantity must be >= 0 (0 removes the item)")
    @Schema(description = "New absolute quantity. 0 = remove the item.", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private int quantity;
}
