package com.app.ecommerce.service.framework;


import java.util.Set;

import com.app.ecommerce.entity.CartItem;
import com.app.ecommerce.models.request.PostCartItemRequestBody;


public interface ICartItemService {

	CartItem createNewCartItem(PostCartItemRequestBody cartItemRequestBody);
	
	Set<CartItem> createNewCartItemsSet(Set<PostCartItemRequestBody> cartItemsSetRequestBody);
}
