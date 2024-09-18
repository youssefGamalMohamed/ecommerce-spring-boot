package com.app.ecommerce.mappers;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.app.ecommerce.dtos.CartItemDto;
import com.app.ecommerce.entity.CartItem;

@Mapper(uses = {ProductMapper.class})
public interface CartItemMapper {

    CartItemMapper INSTANCE = Mappers.getMapper(CartItemMapper.class);

    CartItem mapToEntity(CartItemDto cartItemDto);  
    CartItemDto mapToDto(CartItem cartItem);
    List<CartItemDto> mapToDtos(List<CartItem> cartItems);
    Set<CartItemDto> mapToDtos(Set<CartItem> cartItemDtos);
}
