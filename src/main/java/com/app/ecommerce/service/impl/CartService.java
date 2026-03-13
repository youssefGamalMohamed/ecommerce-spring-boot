package com.app.ecommerce.service.impl;


import com.app.ecommerce.entity.Cart;
import com.app.ecommerce.repository.CartRepo;
import com.app.ecommerce.service.framework.ICartService;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartService implements ICartService {
	
	private final CartRepo cartRepo;
	
	@Override
	public Cart findById(UUID cartId) {
		log.info("findById({})", cartId);
		return cartRepo.findById(cartId)
				.orElseThrow(() -> new NoSuchElementException("Cart with id " + cartId + " not found"));
	}

}
