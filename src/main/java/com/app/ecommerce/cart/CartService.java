package com.app.ecommerce.cart;

import java.util.UUID;

public interface CartService {
    Cart findById(UUID cartId);
}
