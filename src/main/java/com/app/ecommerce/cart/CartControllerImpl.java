package com.app.ecommerce.cart;

import com.app.ecommerce.auth.User;
import com.app.ecommerce.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/carts")
public class CartControllerImpl implements CartController {

    private final CartService cartService;

    @GetMapping
    @Override
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CartResponse>> getCurrentCart(@AuthenticationPrincipal User currentUser) {
        log.info("getCurrentCart(user={})", currentUser.getUsername());
        CartResponse cart = cartService.getCurrentCart(currentUser);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    @Override
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@AuthenticationPrincipal User currentUser,
                                                               @Valid @RequestBody AddCartItemRequest request) {
        log.info("addItem(user={}, request={})", currentUser.getUsername(), request);
        CartResponse cart = cartService.addItem(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(cart));
    }

    @PatchMapping("/items/{cartItemId}")
    @Override
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(@AuthenticationPrincipal User currentUser,
                                                                         @PathVariable("cartItemId") UUID cartItemId,
                                                                         @Valid @RequestBody UpdateCartItemQuantityRequest request) {
        log.info("updateItemQuantity(user={}, cartItemId={}, quantity={})", currentUser.getUsername(), cartItemId, request.getQuantity());
        CartResponse cart = cartService.updateItemQuantity(currentUser, cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart item updated successfully"));
    }

    @DeleteMapping("/items/{cartItemId}")
    @Override
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<Void> removeItem(@AuthenticationPrincipal User currentUser,
                                            @PathVariable("cartItemId") UUID cartItemId) {
        log.info("removeItem(user={}, cartItemId={})", currentUser.getUsername(), cartItemId);
        cartService.removeItem(currentUser, cartItemId);
        return ResponseEntity.noContent().build();
    }
}
