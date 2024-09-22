package com.app.ecommerce.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.ecommerce.entity.Cart;
import com.app.ecommerce.service.framework.ICartItemService;
import com.app.ecommerce.service.framework.ICartService;

@Service
public class CartService implements ICartService {
	
	
	@Autowired
	private ICartItemService cartItemService;
	
	@Override
	public Cart createNewCart(Cart cartRequestBody) {		
		
		Cart cart = Cart.builder()
						.cartItems(
								cartItemService.createNewCartItemsSet(cartRequestBody.getCartItems())
						)
						.build();
		
		cart.getCartItems().forEach(cartItem -> cartItem.setCart(cart));
		
		return cart;
	}

}
