package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.CartItem;

public interface ICartItemService {

	CartItem findById(Long cartItemId);
}
