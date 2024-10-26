package com.youssefgamal.order_service.mappers;

import java.util.Collection;

import org.mapstruct.Mapper;

import com.youssefgamal.order_service.dtos.CartInput;
import com.youssefgamal.order_service.entity.Cart;


@Mapper(componentModel = "spring")
public interface CartMapper {

	CartInput mapToDto(Cart cart);
	Cart mapToEntity(CartInput cartDto);
	Collection<Cart> mapToEntities(Collection<Cart> carts);
	Collection<CartInput> mapToDtos(Collection<CartInput> cartDtos);
	
}
