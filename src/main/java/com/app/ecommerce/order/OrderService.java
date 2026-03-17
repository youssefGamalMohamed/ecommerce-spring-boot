package com.app.ecommerce.order;

import com.app.ecommerce.shared.enums.PaymentType;
import com.app.ecommerce.shared.enums.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface OrderService {

    OrderDto createNewOrder(OrderDto orderDto) throws JsonProcessingException;

    OrderDto findById(UUID orderId);

    void updateOrder(UUID orderId, OrderDto orderDto);

    Page<OrderDto> findAll(Status status, PaymentType paymentType, Instant createdAfter, Instant createdBefore, Pageable pageable);
}
