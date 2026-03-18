package com.app.ecommerce.cart;

import java.util.UUID;

public interface CartItemService {

    CartItemResponse findById(UUID cartItemId);
}
