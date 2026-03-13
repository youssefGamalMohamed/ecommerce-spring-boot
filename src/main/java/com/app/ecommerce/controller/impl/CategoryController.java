package com.app.ecommerce.controller.impl;

import com.app.ecommerce.controller.framework.ICategoryController;
import com.app.ecommerce.dtos.ApiResponseDto;
import com.app.ecommerce.dtos.CategoryDto;
import com.app.ecommerce.entity.Category;
import com.app.ecommerce.mappers.CategoryMapper;
import com.app.ecommerce.service.framework.ICategoryService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RequiredArgsConstructor
@RestController
public class CategoryController implements ICategoryController {


    private final ICategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping("/categories")
    @Override
    public ResponseEntity<ApiResponseDto<CategoryDto>> save(@Valid @RequestBody CategoryDto categoryDto) {
        log.info("save({})", categoryDto);
    	Category newCreatedCategory = categoryService.save(categoryMapper.mapToEntity(categoryDto));
    	
        return new ResponseEntity<>(
        		ApiResponseDto.created(categoryMapper.mapToDto(newCreatedCategory)),
        		HttpStatus.CREATED
        );
    }

    @DeleteMapping("/categories/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteById(@PathVariable(name = "id") UUID categoryId) {
        log.info("deleteById({})", categoryId);
    	categoryService.deleteById(categoryId);
    	return new ResponseEntity<>(
    			ApiResponseDto.noContent(),
    		 	HttpStatus.NO_CONTENT
    	);
    }

    @GetMapping("/categories")
    @Override
    public ResponseEntity<ApiResponseDto<?>> findAll() {
        log.info("findAll()");
        return ResponseEntity.ok(ApiResponseDto.success(categoryMapper.mapToDtos(categoryService.findAll())));
    }

    @GetMapping("/categories/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<CategoryDto>> findById(@PathVariable("id") UUID categoryId) {
        log.info("findById({})", categoryId);
        return ResponseEntity.ok(ApiResponseDto.success(categoryMapper.mapToDto(categoryService.findById(categoryId))));
    }

    @PutMapping("/categories/{id}")
    @Override
    public ResponseEntity<ApiResponseDto<CategoryDto>> updateById(@PathVariable("id") UUID categoryId , @Valid @RequestBody CategoryDto updatedBody) {
        log.info("updateById({}, {})", categoryId, updatedBody);
        return ResponseEntity.ok(ApiResponseDto.success(
                categoryMapper.mapToDto(categoryService.updateById(categoryId, categoryMapper.mapToEntity(updatedBody))),
                "Category updated successfully"));
    }



}
