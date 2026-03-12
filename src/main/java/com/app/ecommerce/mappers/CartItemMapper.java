package com.app.ecommerce.mappers;

import com.app.ecommerce.dtos.CartItemDto;
import com.app.ecommerce.entity.CartItem;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { ProductMapper.class })
public interface CartItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "productQuantity", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CartItem mapToEntity(CartItemDto cartItemDto);

    CartItemDto mapToDto(CartItem cartItem);

    List<CartItemDto> mapToDtos(List<CartItem> cartItems);

    Set<CartItemDto> mapToDtos(Set<CartItem> cartItemDtos);
}
