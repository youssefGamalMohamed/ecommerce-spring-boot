package com.youssefgamal.order_service.mappers;

import java.util.Collection;

import org.mapstruct.Mapper;

import com.youssefgamal.order_service.dtos.OrderDto;
import com.youssefgamal.order_service.entity.Order;


@Mapper(componentModel = "spring", uses = CartMapper.class)
public interface OrderMapper {


	OrderDto mapToDto(Order Order);	
	Order mapToEntity(OrderDto OrderDto);
	Collection<Order> mapToEntities(Collection<OrderDto> OrderDtos);
	Collection<OrderDto> mapToDtos(Collection<Order> Orders);
	
}
