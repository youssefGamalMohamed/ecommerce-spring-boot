package com.youssefgamal.categoryservice.controller.impl;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.youssefgamal.categoryservice.controller.framework.ICategoryController;
import com.youssefgamal.categoryservice.dtos.CategoryDto;
import com.youssefgamal.categoryservice.entity.Category;
import com.youssefgamal.categoryservice.mappers.CategoryMapper;
import com.youssefgamal.categoryservice.service.framework.ICategoryService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
public class CategoryController implements ICategoryController {


    @Autowired
    private ICategoryService categoryService;

//    @RolesAllowed({"ADMIN"})
    @PostMapping("/categories")
    @Override
    public ResponseEntity<?> save(@Valid @RequestBody CategoryDto categoryDto) {
    	Category newCreatedCategory = categoryService.save(CategoryMapper.INSTANCE.mapToEntity(categoryDto));
        return ResponseEntity.created(URI.create("/categories/"+newCreatedCategory.getId()))
        		.body(CategoryMapper.INSTANCE.mapToDto(newCreatedCategory));
    }

//    @RolesAllowed({"ADMIN"})
    @DeleteMapping("/categories/{id}")
    @Override
    public ResponseEntity<?> deleteById(@PathVariable(name = "id") Long categoryId) {
    	categoryService.deleteById(categoryId);
    	return ResponseEntity.noContent()
    			.build();
    }

//    @RolesAllowed({"ADMIN" , "USER"})
    @GetMapping("/categories")
    @Override
    public ResponseEntity<?> findAll() {
    	log.info("findAll(): start");
    	
    	List<CategoryDto> categoryDtos = CategoryMapper.INSTANCE.mapToDtos(categoryService.findAll());
    	log.info("findAll(): {}", categoryDtos);
        return ResponseEntity.ok(categoryDtos);
    }

//    @RolesAllowed({"ADMIN" , "USER"})
    @GetMapping("/categories/{id}")
    @Override
    public ResponseEntity<?> findById(@PathVariable("id") Long categoryId) {
        return ResponseEntity.ok(CategoryMapper.INSTANCE.mapToDto(categoryService.findById(categoryId)));
    }

//    @RolesAllowed({"ADMIN"})
    @PutMapping("/categories/{id}")
    @Override
    public ResponseEntity<?> updateById(@PathVariable("id") Long categoryId , @Valid @RequestBody CategoryDto updatedBody) {
        return ResponseEntity.ok(categoryService.updateById(categoryId, CategoryMapper.INSTANCE.mapToEntity(updatedBody)));
    }



}
