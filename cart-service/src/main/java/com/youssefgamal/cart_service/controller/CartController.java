package com.youssefgamal.cart_service.controller;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.youssefgamal.cart_service.dtos.CartInput;
import com.youssefgamal.cart_service.entity.Cart;
import com.youssefgamal.cart_service.mappers.CartMapper;
import com.youssefgamal.cart_service.services.CartServiceIfc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CartController {

	private final CartServiceIfc cartServiceIfc;
	private final CartMapper cartMapper;
	
	
	@MutationMapping(name = "createCart")
	public Cart createCart(@Argument(name = "cart") CartInput cartInput) {
		log.info("createCart({})", cartInput);
		Cart newCreatedCart = cartServiceIfc.creatCart(cartMapper.mapToEntity(cartInput));
		log.info("createCart({})", newCreatedCart);
		return newCreatedCart;
	}
	
	
	@QueryMapping
	public Cart findById(@Argument Long id) {
		log.info("findById({})", id);
		Cart cart = cartServiceIfc.findById(id);
		log.info("findById({}), cart: {}", id, cart);
		return cart; 
	}
}
