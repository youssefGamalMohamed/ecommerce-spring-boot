package com.app.ecommerce.order;

import com.app.ecommerce.cart.Cart;
import com.app.ecommerce.cart.CartItem;
import com.app.ecommerce.cart.CartRepository;
import com.app.ecommerce.product.Product;
import com.app.ecommerce.shared.constants.CacheConstants;
import com.app.ecommerce.shared.enums.PaymentType;
import com.app.ecommerce.shared.enums.Status;
import com.app.ecommerce.shared.exception.InvalidStateTransitionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
    public OrderResponse createNewOrder(CreateOrderRequest request) throws JsonProcessingException {
        log.info("createNewOrder({})", request);

        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new NoSuchElementException("Cart with id " + request.getCartId() + " not found"));

        BigDecimal totalPrice = BigDecimal.ZERO;
        if (cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                Product product = item.getProduct();
                totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(item.getProductQuantity())));
            }
        }

        Order order = orderMapper.mapToEntity(request);
        order.setTotalPrice(totalPrice);
        order.setCart(cart);
        Order createdOrder = orderRepository.save(order);
        log.info("order created with id = {}", createdOrder.getId());
        return orderMapper.mapToResponse(createdOrder);
    }

    @Override
    @Cacheable(value = CacheConstants.ORDERS, key = "#orderId")
    @Transactional(readOnly = true)
    public OrderResponse findById(UUID orderId) {
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
        return orderMapper.mapToResponse(order);
    }

    @Override
    @CachePut(value = CacheConstants.ORDERS, key = "#result.id")
    @Transactional
    public OrderResponse updateOrder(UUID orderId, UpdateOrderRequest request) {
        log.info("updateOrder({}, {})", orderId, request);
        if (orderId == null) {
            throw new IllegalArgumentException("Order Id Not Exist to Update");
        }

        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order with id " + orderId + " not found"));

        existingOrder.setVersion(request.getVersion());

        if (request.getDeliveryStatus() != null) {
            Status currentStatus = existingOrder.getDeliveryInfo().getStatus();
            Status requestedStatus = request.getDeliveryStatus();
            
            if (!currentStatus.canTransitionTo(requestedStatus)) {
                throw new InvalidStateTransitionException(currentStatus, requestedStatus);
            }
        }

        orderMapper.updateEntityFromRequest(request, existingOrder);

        Order updatedOrder = orderRepository.save(existingOrder);
        log.info("order updated with id = {}", updatedOrder.getId());
        return orderMapper.mapToResponse(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(Status status, PaymentType paymentType, Instant createdAfter, Instant createdBefore, Pageable pageable) {
        log.info("findAll(status={}, paymentType={}, createdAfter={}, createdBefore={}, pageable={})",
                status, paymentType, createdAfter, createdBefore, pageable);

        if (createdAfter != null && createdBefore != null && createdAfter.isAfter(createdBefore)) {
            throw new IllegalArgumentException("createdAfter must be less than or equal to createdBefore");
        }

        Sort safeSort = sanitizeSort(pageable.getSort());
        PageRequest safePage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);

        Specification<Order> spec = Specification
                .where((Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null)
                .and(OrderSpecifications.hasStatus(status))
                .and(OrderSpecifications.hasPaymentType(paymentType))
                .and(OrderSpecifications.createdAfter(createdAfter))
                .and(OrderSpecifications.createdBefore(createdBefore));

        Page<OrderResponse> result = orderRepository.findAll(spec, safePage).map(orderMapper::mapToResponse);
        log.info("findAll(): Found {} orders", result.getTotalElements());
        return result;
    }

    private Sort sanitizeSort(Sort sort) {
        Set<String> allowedFields = Set.of("totalPrice", "createdAt");
        if (sort == null || sort.isUnsorted()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Sort.Order[] orders = sort.get().map(order -> {
            if (allowedFields.contains(order.getProperty())) {
                return order;
            }
            return Sort.Order.desc("createdAt");
        }).toArray(Sort.Order[]::new);
        return Sort.by(orders);
    }

}
