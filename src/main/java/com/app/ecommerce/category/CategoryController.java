package com.app.ecommerce.category;

import com.app.ecommerce.shared.dto.ApiResponseDto;
import com.app.ecommerce.shared.dto.ErrorResponseDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Categories", description = "Category management operations")
public interface CategoryController {

    @Operation(summary = "Add New Category", description = "Create a new category")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "Category created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409", description = "Category already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    }
    )
    ResponseEntity<ApiResponseDto<CategoryResponse>> save(@Valid @RequestBody CreateCategoryRequest request);

    @Operation(summary = "Delete Category By ID", description = "Delete a category by its ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204", description = "Category deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    }
    )
    ResponseEntity<ApiResponseDto<Void>> deleteById(@PathVariable(name = "id") UUID categoryId);

    @Operation(summary = "List Categories", description = "Retrieve categories with optional filtering, sorting, and pagination.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Categories retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            )
    }
    )
    ResponseEntity<ApiResponseDto<Page<CategoryResponse>>> findAll(
            @RequestParam(required = false) String name,
            @ParameterObject @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable);

    @Operation(summary = "Find Category By ID", description = "Retrieve a category by its ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Category retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    }
    )
    ResponseEntity<ApiResponseDto<CategoryResponse>> findById(@PathVariable("id") UUID categoryId);

    @Operation(summary = "Update Category By ID", description = "Update an existing category by its ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Category updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation Error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409", description = "Conflict - Version mismatch",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    }
    )
    ResponseEntity<ApiResponseDto<CategoryResponse>> updateById(@PathVariable("id") UUID categoryId, @Valid @RequestBody UpdateCategoryRequest request);

}
