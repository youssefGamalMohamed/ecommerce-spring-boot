package com.app.ecommerce.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.ecommerce.entity.Cart;
import com.app.ecommerce.entity.CartItem;
import com.app.ecommerce.models.request.PostCartRequestBody;
import com.app.ecommerce.repository.ProductRepo;
import com.app.ecommerce.service.framework.ICartItemService;
import com.app.ecommerce.service.framework.ICartService;

@Service
public class CartService implements ICartService {
	
	
	@Autowired
	private ICartItemService cartItemService;
	
	@Override
	public Cart createNewCart(PostCartRequestBody cartRequestBody) {
//		Set<CartItem> cartItems = new HashSet<>();
//		
//		cartRequestBody.getCartItems().forEach(item -> {
//			cartItems.add(
//						CartItem.builder()
//						.product(productRepo.findById(item.getProductId()).get())
//						.productQuantity(item.getQuantity())
//						.build()
//					);
//		});
		
		
		
		Cart cart = Cart.builder()
						.cartItems(
								cartItemService.createNewCartItemsSet(cartRequestBody.getCartItems())
						)
						.build();
		
		cart.getCartItems().forEach(cartItem -> cartItem.setCart(cart));
		
		return cart;
	}

}
