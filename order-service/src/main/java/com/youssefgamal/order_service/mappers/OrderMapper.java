package com.youssefgamal.order_service.mappers;

import java.util.Collection;

import org.mapstruct.Mapper;

import com.youssefgamal.order_service.dtos.OrderInput;
import com.youssefgamal.order_service.entity.Order;


@Mapper(componentModel = "spring", uses = CartMapper.class)
public interface OrderMapper {


	OrderInput mapToDto(Order Order);	
	Order mapToEntity(OrderInput OrderDto);
	Collection<Order> mapToEntities(Collection<OrderInput> OrderDtos);
	Collection<OrderInput> mapToDtos(Collection<Order> Orders);
	
}
