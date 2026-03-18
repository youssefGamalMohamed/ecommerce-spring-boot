package com.app.ecommerce.order;

import com.app.ecommerce.shared.enums.PaymentType;
import com.app.ecommerce.shared.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface OrderService {

    OrderResponse createNewOrder(CreateOrderRequest request);

    OrderResponse findById(UUID orderId);

    OrderResponse updateOrder(UUID orderId, UpdateOrderRequest request);

    Page<OrderResponse> findAll(Status status, PaymentType paymentType, Instant createdAfter, Instant createdBefore, Pageable pageable);
}
