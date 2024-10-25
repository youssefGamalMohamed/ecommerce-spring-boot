package com.youssefgamal.cart_service.services;

import com.youssefgamal.cart_service.entity.Cart;

public interface CartServiceIfc {
	
	Cart creatCart(Cart cart);

	Cart findById(Long id);
	
	
}
