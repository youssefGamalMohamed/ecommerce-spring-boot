package com.app.ecommerce.order;

import com.app.ecommerce.shared.dto.ApiResponseDto;
import com.app.ecommerce.shared.enums.PaymentType;
import com.app.ecommerce.shared.enums.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    @PostMapping
    @Override
    public ResponseEntity<ApiResponseDto<OrderDto>> createNewOrder(@RequestBody OrderDto orderDto) throws JsonProcessingException {
        log.info("createNewOrder({})", orderDto);
        OrderDto createdOrder = orderService.createNewOrder(orderDto);

        return new ResponseEntity<>(
                ApiResponseDto.created(createdOrder),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Override
    public ResponseEntity<ApiResponseDto<Page<OrderDto>>> findAll(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) PaymentType paymentType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("findAll(status={}, paymentType={}, createdAfter={}, createdBefore={}, pageable={})",
                status, paymentType, createdAfter, createdBefore, pageable);
        Page<OrderDto> page = orderService.findAll(status, paymentType, createdAfter, createdBefore, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(page));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<Void>> updateOrder(@PathVariable("id") UUID orderId, @RequestBody OrderDto orderDto) {
        log.info("updateOrder({}, {})", orderId, orderDto);
        orderService.updateOrder(orderId, orderDto);

        return new ResponseEntity<>(ApiResponseDto.noContent(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<OrderDto>> findOrderById(@PathVariable("id") UUID orderId) {
        log.info("findOrderById({})", orderId);
        OrderDto order = orderService.findById(orderId);

        return new ResponseEntity<>(
                ApiResponseDto.success(order),
                HttpStatus.OK
        );
    }
}
