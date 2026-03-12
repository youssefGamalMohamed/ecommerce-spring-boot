package com.app.ecommerce.mappers;

import com.app.ecommerce.dtos.CartDto;
import com.app.ecommerce.entity.Cart;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { CartItemMapper.class })
public interface CartMapper {

    @Mapping(target = "order", ignore = true)
    Cart mapToEntity(CartDto cartDto);

    CartDto mapToDto(Cart cart);

    List<CartDto> mapToDtos(List<Cart> carts);

    Set<CartDto> mapToDtos(Set<Cart> carts);
}
