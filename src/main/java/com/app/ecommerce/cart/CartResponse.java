package com.app.ecommerce.cart;

import com.app.ecommerce.shared.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Cart response")
public class CartResponse extends BaseResponse {

    @Schema(description = "Unique identifier of the cart", accessMode = Schema.AccessMode.READ_ONLY, example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Current status of the cart", example = "OPEN")
    private CartStatus status;

    @Schema(description = "Set of items in the cart")
    @Builder.Default
    private Set<CartItemResponse> cartItems = new HashSet<>();
}
