package com.app.ecommerce.controller.framework;

import com.app.ecommerce.dtos.OrderDto;
import com.app.ecommerce.dtos.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;


@Tag(name = "Customer", description = "contains All Customer operations")
public interface ICustomerController {



    @Operation(summary = "Find All Orders For Customer By Customer ID , this endpoint accessed for ( User )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Orders Retrieved Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDto.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404", description = "Customer Not Found with this ID"
            )
    }
    )
    ResponseEntity<?> finAllOrdersForCustomerById(@PathVariable("id") Long customerId);



    @Operation(summary = "Find All Customers , this endpoint accessed for ( Customer )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Customers Retrieved Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserDto[].class)
                            )
                    }
            )
    }
    )
    ResponseEntity<?> finAllCustomers();
}
