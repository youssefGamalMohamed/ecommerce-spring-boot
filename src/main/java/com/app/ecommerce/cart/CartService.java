package com.app.ecommerce.cart;

import java.util.UUID;

public interface CartService {
    CartDto findById(UUID cartId);
}
