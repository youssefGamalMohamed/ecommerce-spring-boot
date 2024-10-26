package com.youssefgamal.order_service.controller;


import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.youssefgamal.order_service.dtos.OrderInput;
import com.youssefgamal.order_service.entity.Order;
import com.youssefgamal.order_service.mappers.OrderMapper;
import com.youssefgamal.order_service.service.OrderServiceIfc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderServiceIfc orderServiceIfc;
    private final OrderMapper orderMapper;

    @MutationMapping(name = "createOrder")
    public Order createOrder(@Argument(name = "order") OrderInput orderInput) {
        log.info("createOrder({})", orderInput);
        Order newCreatedOrder = orderServiceIfc.createOrder(orderMapper.mapToEntity(orderInput));
        log.info("createOrder({})", newCreatedOrder);
        return newCreatedOrder;
    }

    @QueryMapping(name = "findOrderById")
    public Order findById(@Argument Long id) {
        log.info("findById({})", id);
        Order order = orderServiceIfc.findById(id);
        log.info("findById({}), Order: {}", id, order);
        return order;
    }
}
