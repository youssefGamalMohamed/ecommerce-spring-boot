package com.app.ecommerce.controller.framework;

import com.app.ecommerce.models.request.PostOrderRequestBody;
import com.app.ecommerce.models.response.http.BadRequestResponse;
import com.app.ecommerce.models.response.http.NotFoundResponse;
import com.app.ecommerce.models.response.endpoints.*;
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
                                    schema = @Schema(implementation = CreateNewOrderResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = BadRequestResponse.class))
                    }
            ),
            @ApiResponse(
                    responseCode = "404", description = "Customer Email Not Found to make this Order for this Customer Email",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = NotFoundResponse.class))
                    }
            )
    }
    )
    ResponseEntity<?> createNewOrder(@Valid @RequestBody PostOrderRequestBody orderRequestBody);


    @Operation(summary = "Find Order By Id , this endpoint accessed for ( Admin , User )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Order Retrieved Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = GetOrderByIdResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404", description = "Order Not Found with this ID",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = NotFoundResponse.class))
                    }
            )
    }
    )
    ResponseEntity<?> findOrderById(@PathVariable("id") Long orderId);

}
