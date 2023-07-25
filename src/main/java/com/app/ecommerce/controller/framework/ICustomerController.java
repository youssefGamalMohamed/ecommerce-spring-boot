package com.app.ecommerce.controller.framework;

import com.app.ecommerce.models.request.PostOrderRequestBody;
import com.app.ecommerce.models.response.endpoints.CreateNewOrderResponse;
import com.app.ecommerce.models.response.endpoints.GetCustomerOrdersResponseBody;
import com.app.ecommerce.models.response.endpoints.GetOrderByIdResponse;
import com.app.ecommerce.models.response.http.BadRequestResponse;
import com.app.ecommerce.models.response.http.NotFoundResponse;
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


@Tag(name = "Customer", description = "contains All Customer operations")
public interface ICustomerController {



    @Operation(summary = "Find All Orders For Customer By Customer ID , this endpoint accessed for ( User )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Orders Retrieved Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = GetCustomerOrdersResponseBody.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404", description = "Customer Not Found with this ID",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = NotFoundResponse.class))
                    }
            )
    }
    )
    ResponseEntity<?> finAllOrdersForCustomerById(@PathVariable("id") Long customerId);

}
