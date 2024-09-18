package com.app.ecommerce.mappers;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.app.ecommerce.dtos.OrderDto;
import com.app.ecommerce.entity.Order;

@Mapper(uses = {DeliveryInfoMapper.class,CartMapper.class,UserMapper.class})
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    Order mapToEntity(OrderDto OrderDto);  
    OrderDto mapToDto(Order Order);
    List<OrderDto> mapToDtos(List<Order> Orders);
    Set<OrderDto> mapToDtos(Set<Order> OrderDtos);
}
