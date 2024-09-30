package com.app.ecommerce.mappers;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.app.ecommerce.dtos.CartDto;
import com.app.ecommerce.entity.Cart;

@Mapper(uses = {CartItemMapper.class})
public interface CartMapper {

    CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    Cart mapToEntity(CartDto CartDto);  
    CartDto mapToDto(Cart Cart);
    List<CartDto> mapToDtos(List<Cart> carts);
    Set<CartDto> mapToDtos(Set<Cart> cartDtos);
}
