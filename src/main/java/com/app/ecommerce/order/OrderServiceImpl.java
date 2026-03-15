package com.app.ecommerce.order;

import com.app.ecommerce.shared.constants.CacheConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @CacheEvict(value = CacheConstants.ORDERS, allEntries = true)
    public OrderDto createNewOrder(OrderDto orderDto) throws JsonProcessingException {
        log.info("createNewOrder({})", orderDto);
        Order order = orderMapper.mapToEntity(orderDto);
        Order createdOrder = orderRepository.save(order);
        log.info("order created with id = {}", createdOrder.getId());
        return orderMapper.mapToDto(createdOrder);
    }

    @Override
    @Cacheable(value = CacheConstants.ORDERS, key = "#orderId")
    public OrderDto findById(UUID orderId) {
        log.info("findById({})", orderId);
        if (orderId == null) {
            log.warn("Attempted to find order with null ID.");
            throw new IllegalArgumentException("orderId == null");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order with ID {} not found.", orderId);
                    return new NoSuchElementException("No Such Order With This Id , Id Not Found with value = " + orderId);
                });

        log.info("order found with id = {}", orderId);
        return orderMapper.mapToDto(order);
    }

    @Override
    @CacheEvict(value = CacheConstants.ORDERS, allEntries = true)
    public void updateOrder(UUID orderId, OrderDto orderDto) {
        log.info("updateOrder({}, {})", orderId, orderDto);
        if (orderId == null) {
            throw new IllegalArgumentException("Order Id Not Exist to Update");
        }

        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order with id " + orderId + " not found"));

        Order tempOrder = orderMapper.mapToEntity(orderDto);
        orderMapper.updateFrom(tempOrder, existingOrder);

        orderRepository.save(existingOrder);
    }

}
