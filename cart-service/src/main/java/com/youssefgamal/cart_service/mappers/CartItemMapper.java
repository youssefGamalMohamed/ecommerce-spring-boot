package com.youssefgamal.cart_service.mappers;

import java.util.Collection;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.youssefgamal.cart_service.dtos.CartItemDto;
import com.youssefgamal.cart_service.entity.CartItem;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

	@Mapping(target = "product.id", source = "productId")
	CartItemDto mapToDto(CartItem cartItem);
	
	@Mapping(target = "productId", source = "product.id")
	@Mapping(target = "cart", ignore = true)
	CartItem mapToEntity(CartItemDto cartItemDto);
	
	Collection<CartItem> mapToEntities(Collection<CartItemDto> cartItemDtos);
	Collection<CartItemDto> mapToDtos(Collection<CartItem> cartItems);
	
}
