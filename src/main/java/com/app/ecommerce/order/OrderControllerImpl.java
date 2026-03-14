package com.app.ecommerce.order;

import com.app.ecommerce.shared.dto.ApiResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderControllerImpl implements OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    @Override
    public ResponseEntity<ApiResponseDto<OrderDto>> createNewOrder(@RequestBody OrderDto orderDto) throws JsonProcessingException {
        log.info("createNewOrder({})", orderDto);
        Order order = orderMapper.mapToEntity(orderDto);
        Order createdOrder = orderService.createNewOrder(order);

        return new ResponseEntity<>(
                ApiResponseDto.created(orderMapper.mapToDto(createdOrder)),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<Void>> updateOrder(@PathVariable("id") UUID orderId, @RequestBody OrderDto orderDto) {
        log.info("updateOrder({}, {})", orderId, orderDto);
        Order updatedOrder = orderMapper.mapToEntity(orderDto);
        orderService.updateOrder(orderId, updatedOrder);

        return new ResponseEntity<>(ApiResponseDto.noContent(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<OrderDto>> findOrderById(@PathVariable("id") UUID orderId) {
        log.info("findOrderById({})", orderId);
        Order order = orderService.findById(orderId);

        return new ResponseEntity<>(
                ApiResponseDto.success(orderMapper.mapToDto(order)),
                HttpStatus.OK
        );
    }
}
