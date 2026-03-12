package com.app.ecommerce.mappers;

import com.app.ecommerce.dtos.OrderDto;
import com.app.ecommerce.entity.Order;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {DeliveryInfoMapper.class, CartMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order mapToEntity(OrderDto orderDto);  

    OrderDto mapToDto(Order order);
    List<OrderDto> mapToDtos(List<Order> orders);
    Set<OrderDto> mapToDtos(Set<Order> orders);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFrom(Order updatedOrder, @MappingTarget Order order);
}
