package com.app.ecommerce.order;

import com.app.ecommerce.shared.dto.ApiResponseDto;
import com.app.ecommerce.shared.dto.ErrorResponseDto;
import com.app.ecommerce.shared.enums.PaymentType;
import com.app.ecommerce.shared.enums.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "Orders", description = "Order management operations")
public interface OrderController {

    @Operation(summary = "Create New Order", description = "Create a new order")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Resource not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    }
    )
    ResponseEntity<ApiResponseDto<OrderDto>> createNewOrder(@Valid @RequestBody OrderDto orderDto) throws JsonProcessingException;

    @Operation(summary = "List Orders", description = "Retrieve orders with optional filtering, sorting, and pagination.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error - invalid date range",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    }
    )
    ResponseEntity<ApiResponseDto<Page<OrderDto>>> findAll(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) PaymentType paymentType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable);

    @Operation(summary = "Update Order", description = "Update an existing order by its ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Order updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    }
    )
    @PutMapping("/orders/{id}")
    ResponseEntity<ApiResponseDto<Void>> updateOrder(@PathVariable("id") UUID orderId, @Valid @RequestBody OrderDto orderDto);

    @Operation(summary = "Find Order By ID", description = "Retrieve an order by its ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Order retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    }
    )
    ResponseEntity<ApiResponseDto<OrderDto>> findOrderById(@PathVariable("id") UUID orderId);

}
