package com.youssefgamal.cart_service.services;

import org.springframework.stereotype.Service;

import com.youssefgamal.cart_service.entity.Cart;
import com.youssefgamal.cart_service.repository.CartRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartServiceIfc {

	private final CartRepository cartRepository;

	public Cart creatCart(Cart cart) {
		log.info("creatCart({})", cart);
		cart.getCartItems().forEach(cartItem -> cartItem.setCart(cart));
		cart.getCartItems().forEach(cartItem -> cartItem.getProduct().setCartItem(cartItem));
		Cart newCreatedCart = cartRepository.save(cart);
		log.info("creatCart({})", newCreatedCart);
		return newCreatedCart;
	}

	@Override
	public Cart findById(Long id) {
		log.info("findById({})", id);
		Cart cart = cartRepository.findById(id)
				.orElseThrow();
		log.info("findById({}), cart: {}", id, cart);
		return cart;
	}
	
}
