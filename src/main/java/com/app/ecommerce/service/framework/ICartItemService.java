package com.app.ecommerce.service.framework;


import java.util.Set;

import com.app.ecommerce.entity.CartItem;


public interface ICartItemService {

	CartItem createNewCartItem(CartItem cartItemRequestBody);
	
	Set<CartItem> createNewCartItemsSet(Set<CartItem> cartItemsSetRequestBody);
}
