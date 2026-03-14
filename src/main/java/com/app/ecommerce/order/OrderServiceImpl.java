package com.app.ecommerce.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public Order createNewOrder(Order order) throws JsonProcessingException {
        log.info("createNewOrder({})", order);
        Order createdOrder = orderRepository.save(order);
        log.info("order created with id = {}", createdOrder.getId());
        return createdOrder;
    }

    @Override
    public Order findById(UUID orderId) {
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
        return order;
    }

    @Override
    public void updateOrder(UUID orderId, Order updatedOrder) {
        log.info("updateOrder({}, {})", orderId, updatedOrder);
        if (orderId == null) {
            throw new IllegalArgumentException("Order Id Not Exist to Update");
        }

        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order with id " + orderId + " not found"));

        orderMapper.updateFrom(updatedOrder, existingOrder);

        orderRepository.save(existingOrder);
    }

}
