package com.app.ecommerce.service.framework;


import com.app.ecommerce.entity.Cart;
import java.util.UUID;


public interface ICartService {
	Cart findById(UUID cartId);
}
