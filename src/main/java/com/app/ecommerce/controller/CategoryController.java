package com.app.ecommerce.controller;

import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.PostCategoryRequestBody;
import com.app.ecommerce.service.framework.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class CategoryController {


    @Autowired
    private ICategoryService categoryService;

    @PostMapping("/categories")
    public ResponseEntity<?> addNewCategory(@RequestBody PostCategoryRequestBody categoryRequestBody) {
        return new ResponseEntity<>(
        		    categoryService.add(categoryRequestBody) ,
        		    HttpStatus.CREATED
        		);
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
    
    @GetMapping("/categories/{id}")
    public ResponseEntity<?> findById(@PathVariable("id") Long categoryId) {
        return ResponseEntity.ok(categoryService.findById(categoryId));
    }
}
