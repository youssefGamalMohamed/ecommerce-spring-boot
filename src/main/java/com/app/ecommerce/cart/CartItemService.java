package com.app.ecommerce.cart;

import com.app.ecommerce.auth.User;
import java.util.UUID;

public interface CartItemService {

    CartItemResponse findById(UUID cartItemId, User owner);
}
