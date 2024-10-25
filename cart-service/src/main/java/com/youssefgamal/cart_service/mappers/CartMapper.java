package com.youssefgamal.cart_service.mappers;

import java.util.Collection;

import org.mapstruct.Mapper;

import com.youssefgamal.cart_service.dtos.CartDto;
import com.youssefgamal.cart_service.entity.Cart;

@Mapper(componentModel = "spring", uses = CartItemMapper.class)
public interface CartMapper {

	CartDto mapToDto(Cart cart);
	Cart mapToEntity(CartDto cartDto);
	Collection<Cart> mapToEntities(Collection<Cart> carts);
	Collection<CartDto> mapToDtos(Collection<CartDto> cartDtos);
	
}
