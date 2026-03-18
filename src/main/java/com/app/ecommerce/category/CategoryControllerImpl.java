package com.app.ecommerce.category;

import com.app.ecommerce.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoryControllerImpl implements CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> save(@Valid @RequestBody CreateCategoryRequest request) {
        log.info("save({})", request);
        CategoryResponse newCreatedCategory = categoryService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(newCreatedCategory));
    }

    @DeleteMapping("/{id}")
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable(name = "id") UUID categoryId) {
        log.info("deleteById({})", categoryId);
        categoryService.deleteById(categoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.noContent());
    }

    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> findAll(
            @RequestParam(required = false) String name,
            @ParameterObject @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("findAll(name={}, pageable={})", name, pageable);
        Page<CategoryResponse> page = categoryService.findAll(name, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponse<CategoryResponse>> findById(@PathVariable("id") UUID categoryId) {
        log.info("findById({})", categoryId);
        return ResponseEntity.ok(ApiResponse.success(categoryService.findById(categoryId)));
    }

    @PatchMapping("/{id}")
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateById(@PathVariable("id") UUID categoryId, @Valid @RequestBody UpdateCategoryRequest request) {
        log.info("updateById({}, {})", categoryId, request);
        return ResponseEntity.ok(ApiResponse.success(
                categoryService.updateById(categoryId, request),
                "Category updated successfully"));
    }

}
