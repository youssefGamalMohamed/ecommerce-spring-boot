package com.app.ecommerce.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.app.ecommerce.entity.CartItem;
import com.app.ecommerce.models.request.PostCartItemRequestBody;
import com.app.ecommerce.repository.ProductRepo;
import com.app.ecommerce.service.framework.ICartItemService;

@Service
public class CartItemService implements ICartItemService {
	
	
	@Autowired
	private ProductRepo productRepo;
	
	@Override
	public CartItem createNewCartItem(PostCartItemRequestBody cartItemRequestBody) {
		return CartItem.builder()
				.product(productRepo.findById(cartItemRequestBody.getProductId()).get())
				.productQuantity(cartItemRequestBody.getQuantity())
				.build();
	}

	@Override
	public Set<CartItem> createNewCartItemsSet(Set<PostCartItemRequestBody> cartItemsSetRequestBody) {
		Set<CartItem> cartItems = new HashSet<>();

		cartItemsSetRequestBody.forEach(item -> {
			cartItems.add(this.createNewCartItem(item));
		});
		
		return cartItems;
	}

}
