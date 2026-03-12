package com.app.ecommerce.service.impl;


import com.app.ecommerce.entity.Cart;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.repository.CartRepo;
import com.app.ecommerce.service.framework.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartService implements ICartService {
	
	private final CartRepo cartRepo;
	
	@Override
	public Cart findById(Long cartId) {
		log.info("findById({})", cartId);
		return cartRepo.findById(cartId)
				.orElseThrow(() -> new IdNotFoundException("Cart with id " + cartId + " not found"));
	}

}
