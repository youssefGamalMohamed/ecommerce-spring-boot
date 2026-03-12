package com.app.ecommerce.mappers;

import com.app.ecommerce.dtos.CartDto;
import com.app.ecommerce.entity.Cart;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { CartItemMapper.class })
public interface CartMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cartItems", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Cart mapToEntity(CartDto cartDto);

    CartDto mapToDto(Cart cart);

    List<CartDto> mapToDtos(List<Cart> carts);

    Set<CartDto> mapToDtos(Set<Cart> carts);
}
