package com.youssefgamal.cart_service.mappers;

import java.util.Collection;

import org.mapstruct.Mapper;

import com.youssefgamal.cart_service.dtos.CartInput;
import com.youssefgamal.cart_service.entity.Cart;

@Mapper(componentModel = "spring", uses = CartItemMapper.class)
public interface CartMapper {

	CartInput mapToInput(Cart cart);
	Cart mapToEntity(CartInput cartInput);
	Collection<Cart> mapToEntities(Collection<Cart> carts);
	Collection<CartInput> mapToInputs(Collection<CartInput> cartInputs);
	
}
