package com.app.ecommerce.controller.framework;

import com.app.ecommerce.dtos.OrderDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(name = "Orders", description = "contains All Order operation for Customer")
public interface IOrderController {


    @Operation(summary = "Add New Order , this endpoint accessed only for Customer")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "Order Added Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDto.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error"
            ),
            @ApiResponse(
                    responseCode = "404", description = "Customer Email Not Found to make this Order for this Customer Email"
            )
    }
    )
    ResponseEntity<?> createNewOrder(@Valid @RequestBody OrderDto orderDto) throws JsonProcessingException;


    @Operation(summary = "Find Order By Id , this endpoint accessed for ( Admin , User )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Order Retrieved Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDto.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404", description = "Order Not Found with this ID"
            )
    }
    )
    ResponseEntity<?> findOrderById(@PathVariable("id") Long orderId);

}
