package com.app.ecommerce.cart;

import com.app.ecommerce.auth.User;
import com.app.ecommerce.shared.models.ApiResponse;
import com.app.ecommerce.shared.models.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Cart", description = "Shopping cart management")
public interface CartController {

    @Operation(summary = "Get Current Cart", description = "Returns the authenticated user's open cart. Creates one automatically if none exists.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart retrieved or created",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ResponseEntity<ApiResponse<CartResponse>> getCurrentCart(@AuthenticationPrincipal User currentUser);

    @Operation(summary = "Add Item to Cart", description = "Adds a product to the cart. If the product already exists, its quantity is incremented.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Item added",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cart is already checked out",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<CartResponse>> addItem(@AuthenticationPrincipal User currentUser, @Valid @RequestBody AddCartItemRequest request);

    @Operation(summary = "Update Cart Item Quantity", description = "Sets the absolute quantity of a cart item. Sending quantity=0 removes the item.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item updated",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cart is already checked out",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(@AuthenticationPrincipal User currentUser,
                                                                  @PathVariable("cartItemId") UUID cartItemId,
                                                                  @Valid @RequestBody UpdateCartItemQuantityRequest request);

    @Operation(summary = "Remove Cart Item", description = "Explicitly removes a specific item from the cart.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item removed",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cart is already checked out",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<CartResponse>> removeItem(@AuthenticationPrincipal User currentUser, @PathVariable("cartItemId") UUID cartItemId);
}
