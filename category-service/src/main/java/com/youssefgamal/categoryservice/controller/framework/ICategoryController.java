package com.youssefgamal.categoryservice.controller.framework;

import com.youssefgamal.categoryservice.dtos.CategoryDto;
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

@Tag(name = "Categories", description = "contains All Category operation for Customer and Admin")
public interface ICategoryController {


    @Operation(summary = "Add New Category , this endpoint accessed only for Admin")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "Category Added Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CategoryDto.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "409", description = "Trying to Add Existing Category Name"
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error"
            )
    }
    )
    ResponseEntity<?> save(@Valid @RequestBody CategoryDto categoryDto);


    @Operation(summary = "Delete Category By Id , this endpoint accessed for ( Admin )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204", description = "Category Deleted Successfully"
            ),
            @ApiResponse(
                    responseCode = "404", description = "Category Not Found with this ID"
            )
    }
    )
    ResponseEntity<?> deleteById(@PathVariable(name = "id") Long categoryId);


    @Operation(summary = "Get All Category , this endpoint accessed for ( Admin , User )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Categories Retrieved Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CategoryDto[].class)
                            )
                    }
            )
    }
    )
    ResponseEntity<?> findAll();


    @Operation(summary = "Find Category By Id , this endpoint accessed for ( Admin , User )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Category Retrieved Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CategoryDto.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404", description = "Category Not Found with this ID"
            )
    }
    )
    ResponseEntity<?> findById(@PathVariable("id") Long categoryId);


    @Operation(summary = "Update Category By Id , this endpoint accessed for ( Admin )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Category Updated Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CategoryDto.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404", description = "Category Not Found with this ID"
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error"
            )
    }
    )
    ResponseEntity<?> updateById(@PathVariable("id") Long categoryId , @Valid @RequestBody CategoryDto categoryDto);


}
