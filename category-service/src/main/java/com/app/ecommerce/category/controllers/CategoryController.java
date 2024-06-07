package com.app.ecommerce.category.controllers;

import com.app.ecommerce.category.dtos.CategoryDto;
import com.app.ecommerce.category.entities.Category;
import com.app.ecommerce.category.mappers.CategoryMapper;
import com.app.ecommerce.category.services.CategoryServiceInterface;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryServiceInterface categoryService;

    @PostMapping("/api/v1/categories")
    public ResponseEntity<?> save(@Valid @RequestBody CategoryDto categoryDto) {
        log.info("addNewCategory() : Request Category Dto : {}", categoryDto);
        Category category = CategoryMapper.toEntity(categoryDto);
        category = categoryService.save(category);
        CategoryDto responseBody = CategoryMapper.toDto(category);
        log.info("addNewCategory() : Response Category Dto : {}", responseBody);
        return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
    }

    @DeleteMapping("/api/v1/categories/{id}")
    public ResponseEntity<?> deleteById(@PathVariable(name = "id") Long categoryId) throws NoSuchElementException {
        log.info("deleteById() : categoryId : {}", categoryId);
        categoryService.deleteById(categoryId);
        log.info("deleteById() : categoryId : {} deleted successfully", categoryId);
        return ResponseEntity.noContent()
                .build();
    }

    @GetMapping("/api/v1/categories")
    public ResponseEntity<?> findAll(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        List<Category> categories = categoryService.findAll(page,size);
        Collection<CategoryDto> responseBody = CategoryMapper.toDto(categories);
        log.info("findAll() : Response Category Dto : {}", responseBody);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/api/v1/categories/{id}")
    public ResponseEntity<?> findById(@PathVariable("id") Long categoryId) {
        log.info("findById() : categoryId : {}", categoryId);
        Category category = categoryService.findById(categoryId);
        CategoryDto responseBody = CategoryMapper.toDto(category);
        log.info("findById() : Response Category Dto : {}", responseBody);
        return ResponseEntity.ok(responseBody);
    }

    @PutMapping("/api/v1/categories/{id}")
    public ResponseEntity<?> updateById(@PathVariable("id") Long categoryId , @Valid @RequestBody Category updatedBody) {
        return ResponseEntity.ok(categoryService.updateById(categoryId, updatedBody));
    }
}
