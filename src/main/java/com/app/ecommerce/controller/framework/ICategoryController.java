package com.app.ecommerce.controller.framework;

import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.PostCategoryRequestBody;
import com.app.ecommerce.models.request.PutCategoryRequestBody;
import com.app.ecommerce.models.response.http.BadRequestResponse;
import com.app.ecommerce.models.response.http.ConflictResponse;
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

@Tag(name = "Categories", description = "contains All Category operation for Customer and Admin")
public interface ICategoryController {


    @Operation(summary = "Add New Category , this endpoint accessed only for Admin")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "Category Added Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AddNewCategoryResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "409", description = "Trying to Add Existing Category Name",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ConflictResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = BadRequestResponse.class))
                    }
            )
    }
    )
    ResponseEntity<?> addNewCategory(@Valid @RequestBody PostCategoryRequestBody categoryRequestBody);


    @Operation(summary = "Delete Category By Id , this endpoint accessed for ( Admin )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204", description = "Category Deleted Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DeleteCategoryResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404", description = "Category Not Found with this ID",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = NotFoundResponse.class))
                    }
            )
    }
    )
    ResponseEntity<?> deleteById(@PathVariable(name = "id") Long categoryId) throws IdNotFoundException;


    @Operation(summary = "Get All Category , this endpoint accessed for ( Admin , User )")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Categories Retrieved Successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = GetAllCategoriesResponse.class)
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
                                    schema = @Schema(implementation = GetCategoryByIdResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404", description = "Category Not Found with this ID",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = NotFoundResponse.class))
                    }
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
                                    schema = @Schema(implementation = UpdateCategoryResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404", description = "Category Not Found with this ID",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = NotFoundResponse.class))
                    }
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = BadRequestResponse.class))
                    }
            )
    }
    )
    ResponseEntity<?> updateById(@PathVariable("id") Long categoryId , @Valid @RequestBody PutCategoryRequestBody updatedBody);


}
