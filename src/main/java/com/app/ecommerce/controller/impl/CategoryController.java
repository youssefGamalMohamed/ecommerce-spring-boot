package com.app.ecommerce.controller.impl;

import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.PostCategoryRequestBody;
import com.app.ecommerce.models.request.PutCategoryRequestBody;
import com.app.ecommerce.service.framework.ICategoryService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
public class CategoryController {


    @Autowired
    private ICategoryService categoryService;

    @RolesAllowed({"ADMIN"})
    @PostMapping("/categories")
    public ResponseEntity<?> addNewCategory(@RequestBody PostCategoryRequestBody categoryRequestBody) {
        return new ResponseEntity<>(
        		    categoryService.add(categoryRequestBody) ,
        		    HttpStatus.CREATED
        		);
    }

    @RolesAllowed({"ADMIN"})
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteById(@PathVariable(name = "id") Long categoryId) throws IdNotFoundException {
    	return new ResponseEntity<>(
    			categoryService.deleteById(categoryId) 
    			, HttpStatus.NO_CONTENT
    		);
    }

    @RolesAllowed({"ADMIN" , "USER"})
    @GetMapping("/categories")
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @RolesAllowed({"ADMIN" , "USER"})
    @GetMapping("/categories/{id}")
    public ResponseEntity<?> findById(@PathVariable("id") Long categoryId) {
        return ResponseEntity.ok(categoryService.findById(categoryId));
    }

    @RolesAllowed({"ADMIN"})
    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateById(@PathVariable("id") Long categoryId , @RequestBody PutCategoryRequestBody updatedBody) {
        return ResponseEntity.ok(categoryService.updateByIdef(categoryId, updatedBody));
    }
    
    
    
}
