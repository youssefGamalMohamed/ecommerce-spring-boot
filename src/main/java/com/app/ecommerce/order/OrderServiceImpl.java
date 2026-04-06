package com.app.ecommerce.order;

import com.app.ecommerce.auth.Role;
import com.app.ecommerce.auth.User;
import com.app.ecommerce.cart.Cart;
import com.app.ecommerce.cart.CartItem;
import com.app.ecommerce.cart.CartRepository;
import com.app.ecommerce.cart.CartStatus;
import com.app.ecommerce.product.Product;
import com.app.ecommerce.shared.constants.CacheConstants;
import com.app.ecommerce.shared.enums.PaymentType;
import com.app.ecommerce.shared.enums.Status;
import com.app.ecommerce.shared.exception.InvalidStateTransitionException;
import com.app.ecommerce.shared.util.SortUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartRepository cartRepository;

    @Override
    @CachePut(value = CacheConstants.ORDERS, key = "#result.id")
    @Transactional
    public OrderResponse createNewOrder(CreateOrderRequest request, User currentUser) {
        log.info("createNewOrder(user={}, request={})", currentUser.getUsername(), request);

        Cart cart = cartRepository.findByOwnerAndStatusWithItems(currentUser, CartStatus.OPEN)
                .orElseThrow(() -> new NoSuchElementException("No open cart found for user " + currentUser.getUsername()));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot place an order with an empty cart");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            if (product == null) {
                throw new NoSuchElementException("Cart contains an item with a missing product.");
            }
            if (product.getPrice() == null) {
                throw new IllegalStateException("Product " + product.getId() + " has no price set.");
            }
            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(item.getProductQuantity())));
        }

        Order order = orderMapper.mapToEntity(request);
        order.setTotalPrice(totalPrice);
        order.setCart(cart);
        Order savedOrder = orderRepository.saveAndFlush(order);

        cart.setOrder(savedOrder);
        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.saveAndFlush(cart);

        log.info("Order created with id={}, cart {} transitioned to CHECKED_OUT", savedOrder.getId(), cart.getId());
        return orderMapper.mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(UUID orderId, User currentUser) {
        log.info("findById({}, user={})", orderId, currentUser.getUsername());
        if (orderId == null) {
            throw new IllegalArgumentException("orderId == null");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("No Such Order With This Id, Id Not Found with value = " + orderId));

        if (currentUser.getRole() == Role.CUSTOMER) {
            Cart orderCart = order.getCart();
            if (orderCart == null || !orderCart.getOwner().getId().equals(currentUser.getId())) {
                throw new NoSuchElementException("No Such Order With This Id, Id Not Found with value = " + orderId);
            }
        }

        log.info("order found with id = {}", orderId);
        return orderMapper.mapToResponse(order);
    }

    @Override
    @CachePut(value = CacheConstants.ORDERS, key = "#result.id")
    @Transactional
    public OrderResponse updateOrder(UUID orderId, UpdateOrderRequest request, User currentUser) {
        log.info("updateOrder({}, {}, user={})", orderId, request, currentUser.getUsername());
        if (orderId == null) {
            throw new IllegalArgumentException("Order Id Not Exist to Update");
        }
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order with id " + orderId + " not found"));
        if (request.getDeliveryStatus() != null) {
            var deliveryInfo = existingOrder.getDeliveryInfo();
            if (deliveryInfo == null) {
                throw new IllegalStateException("Order " + orderId + " has no delivery info");
            }
            Status currentStatus = deliveryInfo.getStatus();
            Status requestedStatus = request.getDeliveryStatus();
            if (!currentStatus.canTransitionTo(requestedStatus)) {
                throw new InvalidStateTransitionException(currentStatus, requestedStatus);
            }
        }
        orderMapper.updateEntityFromRequest(request, existingOrder);
        Order updatedOrder = orderRepository.saveAndFlush(existingOrder);
        log.info("order updated with id = {}", updatedOrder.getId());
        return orderMapper.mapToResponse(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(Status status, PaymentType paymentType, Instant createdAfter, Instant createdBefore, Pageable pageable, User currentUser) {
        log.info("findAll(status={}, paymentType={}, createdAfter={}, createdBefore={}, pageable={}, user={})",
                status, paymentType, createdAfter, createdBefore, pageable, currentUser.getUsername());

        if (createdAfter != null && createdBefore != null && createdAfter.isAfter(createdBefore)) {
            throw new IllegalArgumentException("createdAfter must be less than or equal to createdBefore");
        }

        Sort safeSort = SortUtils.sanitize(pageable.getSort(), Set.of("totalPrice", "createdAt"), Sort.Order.desc("createdAt"));
        PageRequest safePage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);

        Specification<Order> spec = Specification
                .where(OrderSpecifications.hasStatus(status))
                .and(OrderSpecifications.hasPaymentType(paymentType))
                .and(OrderSpecifications.createdAfter(createdAfter))
                .and(OrderSpecifications.createdBefore(createdBefore));

        if (currentUser.getRole() == Role.CUSTOMER) {
            spec = spec.and(OrderSpecifications.hasOwner(currentUser));
        }

        Page<OrderResponse> result = orderRepository.findAll(spec, safePage).map(orderMapper::mapToResponse);
        log.info("findAll(user={}): Found {} orders", currentUser.getUsername(), result.getTotalElements());
        return result;
    }

}
