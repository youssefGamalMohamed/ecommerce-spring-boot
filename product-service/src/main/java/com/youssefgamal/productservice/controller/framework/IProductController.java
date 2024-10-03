package com.youssefgamal.productservice.controller.framework;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.youssefgamal.productservice.dtos.ProductDto;
import com.youssefgamal.productservice.exception.type.IdNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Products", description = "contains All Product operation for Customer and Admin")
public interface IProductController {

    @Operation(summary = "Add New Product , this endpoint accessed only for Admin")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "Product Added Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProductDto.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error"
            ),
            @ApiResponse(
                    responseCode = "404", description = "Category Not Found with this ID , to make this Product of this specific Id"
            )
    }
    )
    ResponseEntity<?> save(@Valid @RequestBody ProductDto productDto) throws Exception;

    
    
    @Operation(summary = "Update Product By Id , this endpoint accessed for ( Admin )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Product Updated Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProductDto.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404", description = "Product Not Found with this ID"
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error"
            )
    }
    )
    ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDto productDto);



    @Operation(summary = "Delete Product By Id , this endpoint accessed for ( Admin )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204", description = "Product Deleted Successfully"
            ),
            @ApiResponse(
                    responseCode = "404", description = "Product Not Found with this ID"
            )
    }
    )
    ResponseEntity<?> deleteById(@PathVariable(name = "id") Long productId) throws IdNotFoundException;


    
    @Operation(summary = "Find All Products, this endpoint accessed for ( Admin , User )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Products Retrieved Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProductDto[].class)
                            )
                    }
            )
    }
    )
    ResponseEntity<?> findAll();
    
    
    
    
    @Operation(summary = "Delete All Products By Category Id , this endpoint accessed for ( Admin )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Products Deleted Successfully"
            )
    }
    )
    ResponseEntity<?> deleteByCategoryId(@RequestParam(name = "category_id") Long category_id);
    
    
    
    
    @Operation(summary = "Find Product By Id , this endpoint accessed for ( Admin, Customer )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Product Fetched Successfully"
            ),
            @ApiResponse(
                    responseCode = "404", description = "Product Not Found with this ID"
            )
    }
    )
    ResponseEntity<?> findById(@PathVariable Long id);
}
