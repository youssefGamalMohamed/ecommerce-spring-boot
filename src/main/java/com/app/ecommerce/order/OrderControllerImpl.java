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
