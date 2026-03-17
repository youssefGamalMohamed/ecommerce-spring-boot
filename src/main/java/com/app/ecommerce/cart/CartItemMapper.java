package com.app.ecommerce.cart;

import com.app.ecommerce.product.ProductMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
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
