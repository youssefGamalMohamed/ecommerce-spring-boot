package com.app.ecommerce.controller.framework;

import com.app.ecommerce.dtos.ProductDto;
import com.app.ecommerce.exception.type.IdNotFoundException;
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
import org.springframework.web.bind.annotation.RequestParam;

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
    ResponseEntity<?> save(@Valid @RequestBody ProductDto productDto);


    @Operation(summary = "Find All Products with Specific Category Name , this endpoint accessed for ( Admin , User )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Products Retrieved Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProductDto[].class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404", description = "Category Name Not Found"
            )
    }
    )
    ResponseEntity<?> findProductsByCategoryName(@RequestParam(value = "category") String categoryName);

    
    
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
    ResponseEntity<?> updateProduct(@PathVariable(value = "id") Long productId , @Valid @RequestBody ProductDto productDto);



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


}
