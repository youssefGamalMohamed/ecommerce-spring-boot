package com.app.ecommerce.cart;

import java.util.UUID;

public interface CartService {
    CartResponse findById(UUID cartId);
}
