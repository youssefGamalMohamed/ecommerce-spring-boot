package com.app.ecommerce.cart;

import java.util.UUID;

public interface CartItemService {

    CartItemDto findById(UUID cartItemId);
}
