package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.CartItem;
import java.util.UUID;

public interface ICartItemService {

	CartItem findById(UUID cartItemId);
}
