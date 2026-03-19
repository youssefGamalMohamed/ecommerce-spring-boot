package com.app.ecommerce.order;

import com.app.ecommerce.auth.User;
import com.app.ecommerce.shared.enums.PaymentType;
import com.app.ecommerce.shared.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface OrderService {

    OrderResponse createNewOrder(CreateOrderRequest request, User currentUser);

    OrderResponse findById(UUID orderId, User currentUser);

    OrderResponse updateOrder(UUID orderId, UpdateOrderRequest request, User currentUser);

    Page<OrderResponse> findAll(Status status, PaymentType paymentType, Instant createdAfter, Instant createdBefore, Pageable pageable, User currentUser);
}
