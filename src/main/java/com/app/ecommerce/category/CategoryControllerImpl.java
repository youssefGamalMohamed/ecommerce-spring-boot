package com.app.ecommerce.category;

import com.app.ecommerce.shared.dto.ApiResponseDto;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoryControllerImpl implements CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping
    @Override
    public ResponseEntity<ApiResponseDto<CategoryDto>> save(@Valid @RequestBody CategoryDto categoryDto) {
        log.info("save({})", categoryDto);
        Category newCreatedCategory = categoryService.save(categoryMapper.mapToEntity(categoryDto));

        return new ResponseEntity<>(
                ApiResponseDto.created(categoryMapper.mapToDto(newCreatedCategory)),
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
        return ResponseEntity.ok(ApiResponseDto.success(categoryMapper.mapToDtos(categoryService.findAll())));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<CategoryDto>> findById(@PathVariable("id") UUID categoryId) {
        log.info("findById({})", categoryId);
        return ResponseEntity.ok(ApiResponseDto.success(categoryMapper.mapToDto(categoryService.findById(categoryId))));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<CategoryDto>> updateById(@PathVariable("id") UUID categoryId, @Valid @RequestBody CategoryDto updatedBody) {
        log.info("updateById({}, {})", categoryId, updatedBody);
        return ResponseEntity.ok(ApiResponseDto.success(
                categoryMapper.mapToDto(categoryService.updateById(categoryId, categoryMapper.mapToEntity(updatedBody))),
                "Category updated successfully"));
    }

}
