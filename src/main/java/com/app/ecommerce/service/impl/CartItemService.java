package com.app.ecommerce.service.impl;

import com.app.ecommerce.entity.CartItem;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.repository.CartItemRepo;
import com.app.ecommerce.service.framework.ICartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CartItemService implements ICartItemService {
	
	private final CartItemRepo cartItemRepo;
	
	@Override
	public CartItem findById(Long cartItemId) {
		return cartItemRepo.findById(cartItemId)
				.orElseThrow(() -> new IdNotFoundException("CartItem with id " + cartItemId + " not found"));
	}

}
