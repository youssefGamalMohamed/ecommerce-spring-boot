package com.app.ecommerce.category;

import com.app.ecommerce.shared.dto.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponseDto<CategoryDto>> save(@Valid @RequestBody CategoryDto categoryDto) {
        log.info("save({})", categoryDto);
        CategoryDto newCreatedCategory = categoryService.save(categoryDto);

        return new ResponseEntity<>(
                ApiResponseDto.created(newCreatedCategory),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteById(@PathVariable(name = "id") UUID categoryId) {
        log.info("deleteById({})", categoryId);
        categoryService.deleteById(categoryId);
        return new ResponseEntity<>(
                ApiResponseDto.noContent(),
                HttpStatus.NO_CONTENT
        );
    }

    @GetMapping
    @Override
    public ResponseEntity<ApiResponseDto<?>> findAll() {
        log.info("findAll()");
        return ResponseEntity.ok(ApiResponseDto.success(categoryService.findAll()));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<CategoryDto>> findById(@PathVariable("id") UUID categoryId) {
        log.info("findById({})", categoryId);
        return ResponseEntity.ok(ApiResponseDto.success(categoryService.findById(categoryId)));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<CategoryDto>> updateById(@PathVariable("id") UUID categoryId, @Valid @RequestBody CategoryDto updatedBody) {
        log.info("updateById({}, {})", categoryId, updatedBody);
        return ResponseEntity.ok(ApiResponseDto.success(
                categoryService.updateById(categoryId, updatedBody),
                "Category updated successfully"));
    }

}
