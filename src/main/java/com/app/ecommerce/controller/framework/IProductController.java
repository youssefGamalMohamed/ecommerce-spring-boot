package com.app.ecommerce.controller.framework;

import com.app.ecommerce.dtos.ApiResponseDto;
import com.app.ecommerce.dtos.ErrorResponseDto;
import com.app.ecommerce.dtos.ProductDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Products", description = "Product management operations for Customer and Admin")
public interface IProductController {

    @Operation(summary = "Add New Product", description = "Create a new product. This endpoint is accessible only by Admin.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Category Not Found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409", description = "Conflict - Duplicate data",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    }
    )
    ResponseEntity<ApiResponseDto<ProductDto>> save(@Valid @RequestBody ProductDto productDto);


    @Operation(summary = "Find Products by Category Name", description = "Retrieve all products for a specific category. Accessible by Admin and User.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    }
    )
    ResponseEntity<ApiResponseDto<?>> findProductsByCategoryName(@RequestParam(value = "category") String categoryName);


    @Operation(summary = "Update Product By ID", description = "Update an existing product by its ID. This endpoint is accessible only by Admin.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    }
    )
    ResponseEntity<ApiResponseDto<ProductDto>> updateById(@PathVariable(value = "id") UUID productId , @Valid @RequestBody ProductDto productDto);



    @Operation(summary = "Delete Product By ID", description = "Delete a product by its ID. This endpoint is accessible only by Admin.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204", description = "Product deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    }
    )
    ResponseEntity<ApiResponseDto<Void>> deleteById(@PathVariable(name = "id") UUID productId);


}
