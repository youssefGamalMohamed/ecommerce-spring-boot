package com.app.ecommerce.cart;

import com.app.ecommerce.product.ProductMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {ProductMapper.class}, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CartItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CartItemResponse mapToResponse(CartItem cartItem);
}
