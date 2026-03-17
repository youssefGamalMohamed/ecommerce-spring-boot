package com.app.ecommerce.order;

import com.app.ecommerce.shared.dto.ApiResponseDto;
import com.app.ecommerce.shared.enums.PaymentType;
import com.app.ecommerce.shared.enums.Status;
import com.app.ecommerce.shared.idempotency.IdempotencyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public ResponseEntity<ApiResponseDto<OrderResponse>> createNewOrder(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CreateOrderRequest request) throws JsonProcessingException {
        log.info("createNewOrder({})", request);

        if (idempotencyKey != null) {
            var existingRecord = idempotencyService.findByKey(idempotencyKey);
            if (existingRecord.isPresent()) {
                log.info("Returning cached response for idempotency key: {}", idempotencyKey);
                var record = existingRecord.get();
                ApiResponseDto<OrderResponse> cachedResponse = objectMapper.readValue(
                        record.getResponseBody(),
                        objectMapper.getTypeFactory().constructParametricType(ApiResponseDto.class, OrderResponse.class)
                );
                return ResponseEntity.status(record.getHttpStatus()).body(cachedResponse);
            }
        }

        OrderResponse createdOrder = orderService.createNewOrder(request);
        ApiResponseDto<OrderResponse> response = ApiResponseDto.created(createdOrder);

        if (idempotencyKey != null) {
            idempotencyService.store(idempotencyKey, HttpStatus.CREATED.value(), response);
        }

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Override
    public ResponseEntity<ApiResponseDto<Page<OrderResponse>>> findAll(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) PaymentType paymentType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("findAll(status={}, paymentType={}, createdAfter={}, createdBefore={}, pageable={})",
                status, paymentType, createdAfter, createdBefore, pageable);
        Page<OrderResponse> page = orderService.findAll(status, paymentType, createdAfter, createdBefore, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(page));
    }

    @PatchMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<OrderResponse>> updateOrder(@PathVariable("id") UUID orderId, @RequestBody UpdateOrderRequest request) {
        log.info("updateOrder({}, {})", orderId, request);
        OrderResponse updatedOrder = orderService.updateOrder(orderId, request);

        return new ResponseEntity<>(
                ApiResponseDto.success(updatedOrder, "Order updated successfully"),
                HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<OrderResponse>> findOrderById(@PathVariable("id") UUID orderId) {
        log.info("findOrderById({})", orderId);
        OrderResponse order = orderService.findById(orderId);

        return new ResponseEntity<>(
                ApiResponseDto.success(order),
                HttpStatus.OK
        );
    }
}
