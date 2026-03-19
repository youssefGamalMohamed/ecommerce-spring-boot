package com.app.ecommerce.cart;

import com.app.ecommerce.auth.User;

import java.util.UUID;

public interface CartService {
    CartResponse findById(UUID cartId);

    /**
     * Returns the authenticated user's current OPEN cart.
     * If no OPEN cart exists, one is automatically created and persisted.
     */
    CartResponse getCurrentCart(User owner);

    /**
     * Adds a product to the user's OPEN cart.
     * If the product already exists in the cart, its quantity is incremented
     * by the amount in the request (upsert behavior).
     * Throws NoSuchElementException if the product does not exist.
     * Throws CartNotOpenException if the cart is CHECKED_OUT.
     */
    CartResponse addItem(User owner, AddCartItemRequest request);

    /**
     * Updates the quantity of a specific cart item to the absolute value in the request.
     * If quantity == 0, the item is automatically deleted from the cart.
     * Throws NoSuchElementException if the cart item is not found or does not belong to this user.
     * Throws CartNotOpenException if the cart is CHECKED_OUT.
     */
    CartResponse updateItemQuantity(User owner, UUID cartItemId, UpdateCartItemQuantityRequest request);

    /**
     * Removes a specific cart item from the user's cart.
     * Throws NoSuchElementException if the cart item is not found or does not belong to this user.
     * Throws CartNotOpenException if the cart is CHECKED_OUT.
     */
    CartResponse removeItem(User owner, UUID cartItemId);
}
