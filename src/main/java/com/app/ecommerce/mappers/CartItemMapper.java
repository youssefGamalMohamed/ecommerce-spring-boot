package com.app.ecommerce.mappers;

import com.app.ecommerce.dtos.CartItemDto;
import com.app.ecommerce.entity.CartItem;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = { ProductMapper.class })
public interface CartItemMapper {

    CartItemMapper INSTANCE = Mappers.getMapper(CartItemMapper.class);

    @Mapping(target = "cart", ignore = true)
    CartItem mapToEntity(CartItemDto cartItemDto);

    CartItemDto mapToDto(CartItem cartItem);

    List<CartItemDto> mapToDtos(List<CartItem> cartItems);

    Set<CartItemDto> mapToDtos(Set<CartItem> cartItemDtos);
}
