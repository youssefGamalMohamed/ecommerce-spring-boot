package com.app.ecommerce.order;

import com.app.ecommerce.cart.CartMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {DeliveryInfoMapper.class, CartMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Order mapToEntity(OrderDto orderDto);

    OrderDto mapToDto(Order order);

    List<OrderDto> mapToDtos(List<Order> orders);

    Set<OrderDto> mapToDtos(Set<Order> orders);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateFrom(Order updatedOrder, @MappingTarget Order order);
}
