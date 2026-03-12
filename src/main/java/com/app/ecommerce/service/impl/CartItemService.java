package com.app.ecommerce.service.impl;

import com.app.ecommerce.entity.CartItem;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.repository.CartItemRepo;
import com.app.ecommerce.service.framework.ICartItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartItemService implements ICartItemService {
	
	private final CartItemRepo cartItemRepo;
	
	@Override
	public CartItem findById(Long cartItemId) {
		log.info("findById({})", cartItemId);
		return cartItemRepo.findById(cartItemId)
				.orElseThrow(() -> new IdNotFoundException("CartItem with id " + cartItemId + " not found"));
	}

}
