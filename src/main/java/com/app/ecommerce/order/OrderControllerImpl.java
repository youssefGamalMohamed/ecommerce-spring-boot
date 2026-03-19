package com.app.ecommerce.order;

import com.app.ecommerce.auth.User;
import com.app.ecommerce.shared.models.ApiResponse;
import com.app.ecommerce.shared.enums.PaymentType;
import com.app.ecommerce.shared.enums.Status;
import com.app.ecommerce.shared.idempotency.IdempotencyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderControllerImpl implements OrderController {

    private final OrderService orderService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @PostMapping
    @Override
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> createNewOrder(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal User currentUser) throws JsonProcessingException {
        log.info("createNewOrder(user={}, request={})", currentUser.getUsername(), request);

        if (idempotencyKey != null) {
            var existingRecord = idempotencyService.findByKey(idempotencyKey);
            if (existingRecord.isPresent()) {
                log.info("Returning cached response for idempotency key: {}", idempotencyKey);
                var record = existingRecord.get();
                try {
                    ApiResponse<OrderResponse> cachedResponse = objectMapper.readValue(
                            record.getResponseBody(),
                            objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, OrderResponse.class)
                    );
                    return ResponseEntity.status(record.getHttpStatus()).body(cachedResponse);
                } catch (JsonProcessingException e) {
                    log.error("Failed to parse cached response", e);
                    throw e;
                }
            }
        }

        OrderResponse createdOrder = orderService.createNewOrder(request, currentUser);
        ApiResponse<OrderResponse> response = ApiResponse.created(createdOrder);

        if (idempotencyKey != null) {
            idempotencyService.store(idempotencyKey, HttpStatus.CREATED.value(), response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Override
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> findAll(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) PaymentType paymentType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        log.info("findAll(user={}, status={}, paymentType={})", currentUser.getUsername(), status, paymentType);
        Page<OrderResponse> page = orderService.findAll(status, paymentType, createdAfter, createdBefore, pageable, currentUser);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PatchMapping("/{id}")
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable("id") UUID orderId,
            @Valid @RequestBody UpdateOrderRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("updateOrder({}, user={})", orderId, currentUser.getUsername());
        OrderResponse updatedOrder = orderService.updateOrder(orderId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(updatedOrder, "Order updated successfully"));
    }

    @GetMapping("/{id}")
    @Override
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> findOrderById(
            @PathVariable("id") UUID orderId,
            @AuthenticationPrincipal User currentUser) {
        log.info("findOrderById({}, user={})", orderId, currentUser.getUsername());
        OrderResponse order = orderService.findById(orderId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
}
