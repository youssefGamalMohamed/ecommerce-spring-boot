package com.app.ecommerce.cart;

import com.app.ecommerce.product.ProductDto;
import com.app.ecommerce.shared.dto.BaseDto;
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
@Schema(description = "Cart item data transfer object")
public class CartItemDto extends BaseDto {

    @Schema(description = "Unique identifier of the cart item", accessMode = Schema.AccessMode.READ_ONLY, example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Quantity of the product in the cart", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private int productQuantity;

    @Schema(description = "Product details")
    private ProductDto product;
}
