package com.youssefgamal.cart_service.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.youssefgamal.cart_service.dtos.CartDto;
import com.youssefgamal.cart_service.dtos.CartItemDto;
import com.youssefgamal.cart_service.entity.Cart;
import com.youssefgamal.cart_service.integrations.ProductServiceIfc;
import com.youssefgamal.cart_service.mappers.CartMapper;
import com.youssefgamal.cart_service.services.CartServiceIfc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CartController {

	private final CartServiceIfc cartServiceIfc;
	private final CartMapper cartMapper;
	private final ProductServiceIfc productServiceIfc;
	
	@PostMapping("/carts")
	@ResponseStatus(HttpStatus.CREATED)
	public CartDto createCart(@RequestBody CartDto cartDto) {
		log.info("createCart({})", cartDto);
		Cart newCreatedCart = cartServiceIfc.creatCart(cartMapper.mapToEntity(cartDto));
		CartDto newCreatedCartDto = cartMapper.mapToDto(newCreatedCart);
		log.info("createCart({})", newCreatedCartDto);
		return newCreatedCartDto;
	}
	
	@GetMapping("/carts/{id}")
	@ResponseStatus(HttpStatus.OK)
	public CartDto findById(@PathVariable Long id) {
		log.info("findById({})", id);
		Cart cart = cartServiceIfc.findById(id);
		CartDto cartDto = cartMapper.mapToDto(cart);
		Set<CartItemDto> cartItemDtos = cartDto.getCartItems()
											.stream()
											.peek(cartItemDto -> cartItemDto.setProduct(productServiceIfc.findById(cartItemDto.getProduct().getId())))
											.collect(Collectors.toSet());
		cartDto.setCartItems(cartItemDtos);
		log.info("findById({}), cart: {}", id, cartDto);
		return cartDto; 
	}
}
