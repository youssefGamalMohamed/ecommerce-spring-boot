package com.app.ecommerce.mappers;

import com.app.ecommerce.dtos.CartDto;
import com.app.ecommerce.entity.Cart;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = { CartItemMapper.class })
public interface CartMapper {

    CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    @Mapping(target = "order", ignore = true)
    Cart mapToEntity(CartDto CartDto);

    CartDto mapToDto(Cart Cart);

    List<CartDto> mapToDtos(List<Cart> carts);

    Set<CartDto> mapToDtos(Set<Cart> cartDtos);
}
