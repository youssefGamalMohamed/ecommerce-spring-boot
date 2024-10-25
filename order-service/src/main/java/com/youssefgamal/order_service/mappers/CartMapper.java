package com.youssefgamal.order_service.mappers;

import java.util.Collection;

import org.mapstruct.Mapper;

import com.youssefgamal.order_service.dtos.CartDto;
import com.youssefgamal.order_service.entity.Cart;


@Mapper(componentModel = "spring")
public interface CartMapper {

	CartDto mapToDto(Cart cart);
	Cart mapToEntity(CartDto cartDto);
	Collection<Cart> mapToEntities(Collection<Cart> carts);
	Collection<CartDto> mapToDtos(Collection<CartDto> cartDtos);
	
}
