package com.app.ecommerce.controller.impl;

import com.app.ecommerce.controller.framework.ICategoryController;
import com.app.ecommerce.dtos.CategoryDto;
import com.app.ecommerce.entity.Category;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.mappers.CategoryMapper;
import com.app.ecommerce.service.framework.ICategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
public class CategoryController implements ICategoryController {


    private final ICategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping("/categories")
    @Override
    public ResponseEntity<?> save(@Valid @RequestBody CategoryDto categoryDto) {
    	Category newCreatedCategory = categoryService.save(categoryMapper.mapToEntity(categoryDto));
    	
        return new ResponseEntity<>(
        		     categoryMapper.mapToDto(newCreatedCategory),
        		    HttpStatus.CREATED
        		);
    }

    @DeleteMapping("/categories/{id}")
    @Override
    public ResponseEntity<?> deleteById(@PathVariable(name = "id") Long categoryId) throws IdNotFoundException {
    	categoryService.deleteById(categoryId);
    	return new ResponseEntity<>(
    			 HttpStatus.NO_CONTENT
    		);
    }

    @GetMapping("/categories")
    @Override
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(categoryMapper.mapToDtos(categoryService.findAll()));
    }

    @GetMapping("/categories/{id}")
    @Override
    public ResponseEntity<?> findById(@PathVariable("id") Long categoryId) {
        return ResponseEntity.ok(categoryMapper.mapToDto(categoryService.findById(categoryId)));
    }

    @PutMapping("/categories/{id}")
    @Override
    public ResponseEntity<?> updateById(@PathVariable("id") Long categoryId , @Valid @RequestBody CategoryDto updatedBody) {
        return ResponseEntity.ok(categoryService.updateById(categoryId, categoryMapper.mapToEntity(updatedBody)));
    }



}
