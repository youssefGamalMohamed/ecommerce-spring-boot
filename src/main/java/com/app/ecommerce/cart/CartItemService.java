package com.app.ecommerce.cart;

import java.util.UUID;

public interface CartItemService {

    CartItem findById(UUID cartItemId);
}
