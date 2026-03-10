package com.app.ecommerce.service.framework;


import com.app.ecommerce.entity.CartItem;
import java.util.Set;


public interface ICartItemService {

	CartItem createNewCartItem(CartItem cartItemRequestBody);
	
	Set<CartItem> createNewCartItemsSet(Set<CartItem> cartItemsSetRequestBody);
}
