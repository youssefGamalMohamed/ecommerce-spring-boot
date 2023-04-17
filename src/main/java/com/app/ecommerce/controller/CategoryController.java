package com.app.ecommerce.controller;

import com.app.ecommerce.entity.Category;
import com.app.ecommerce.exception.models.IdNotFoundException;
import com.app.ecommerce.models.request.CategoryDTO;
import com.app.ecommerce.service.framework.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class CategoryController {


    @Autowired
    private ICategoryService categoryService;

    @PostMapping("/category")
    public ResponseEntity<?> addCategory(@RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.addCategory(categoryDTO));
    }

    @DeleteMapping("/category/{id}")
    public ResponseEntity<?> deleteCategoryById(@PathVariable(name = "id") Long categoryId) throws IdNotFoundException {
        return ResponseEntity.ok(categoryService.deleteCategoryById(categoryId));
    }


    @GetMapping("/category")
    public ResponseEntity<?> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}
