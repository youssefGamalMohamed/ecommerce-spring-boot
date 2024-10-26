package com.youssefgamal.cart_service.mappers;

import java.util.Collection;

import org.mapstruct.Mapper;

import com.youssefgamal.cart_service.dtos.CartItemInput;
import com.youssefgamal.cart_service.entity.CartItem;

@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface CartItemMapper {

	
	CartItemInput mapToInput(CartItem cartItem);
	CartItem mapToEntity(CartItemInput cartItemInput);
	Collection<CartItem> mapToEntities(Collection<CartItemInput> cartItemInputs);
	Collection<CartItemInput> mapToInputs(Collection<CartItem> cartItems);
	
}
