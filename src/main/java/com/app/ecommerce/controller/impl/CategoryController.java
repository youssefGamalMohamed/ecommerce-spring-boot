package com.app.ecommerce.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.ecommerce.controller.framework.ICategoryController;
import com.app.ecommerce.dtos.CategoryDto;
import com.app.ecommerce.entity.Category;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.mappers.CategoryMapper;
import com.app.ecommerce.service.framework.ICategoryService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;


@RestController
public class CategoryController implements ICategoryController {


    @Autowired
    private ICategoryService categoryService;

    @RolesAllowed({"ADMIN"})
    @PostMapping("/categories")
    @Override
    public ResponseEntity<?> save(@Valid @RequestBody CategoryDto categoryDto) {
    	Category newCreatedCategory = categoryService.save(CategoryMapper.INSTANCE.mapToEntity(categoryDto));
    	
        return new ResponseEntity<>(
        		     CategoryMapper.INSTANCE.mapToDto(newCreatedCategory),
        		    HttpStatus.CREATED
        		);
    }

    @RolesAllowed({"ADMIN"})
    @DeleteMapping("/categories/{id}")
    @Override
    public ResponseEntity<?> deleteById(@PathVariable(name = "id") Long categoryId) throws IdNotFoundException {
    	categoryService.deleteById(categoryId);
    	return new ResponseEntity<>(
    			 HttpStatus.NO_CONTENT
    		);
    }

    @RolesAllowed({"ADMIN" , "USER"})
    @GetMapping("/categories")
    @Override
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(CategoryMapper.INSTANCE.mapToDtos(categoryService.findAll()));
    }

    @RolesAllowed({"ADMIN" , "USER"})
    @GetMapping("/categories/{id}")
    @Override
    public ResponseEntity<?> findById(@PathVariable("id") Long categoryId) {
        return ResponseEntity.ok(CategoryMapper.INSTANCE.mapToDto(categoryService.findById(categoryId)));
    }

    @RolesAllowed({"ADMIN"})
    @PutMapping("/categories/{id}")
    @Override
    public ResponseEntity<?> updateById(@PathVariable("id") Long categoryId , @Valid @RequestBody CategoryDto updatedBody) {
        return ResponseEntity.ok(categoryService.updateById(categoryId, CategoryMapper.INSTANCE.mapToEntity(updatedBody)));
    }



}
