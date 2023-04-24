package com.app.ecommerce.controller;

import com.app.ecommerce.entity.Category;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.CategoryRequestBody;
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

    @PostMapping("/categories")
    public ResponseEntity<?> addNewCategory(@RequestBody CategoryRequestBody categoryRequestBody) {
        return ResponseEntity.ok(categoryService.add(categoryRequestBody));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteById(@PathVariable(name = "id") Long categoryId) throws IdNotFoundException {
    	return new ResponseEntity<>(
    			categoryService.deleteById(categoryId) 
    			, HttpStatus.NO_CONTENT
    		);
    }


    @GetMapping("/categories")
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }
}
