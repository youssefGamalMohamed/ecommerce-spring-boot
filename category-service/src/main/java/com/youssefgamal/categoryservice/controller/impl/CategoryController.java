package com.youssefgamal.categoryservice.controller.impl;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @Override
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto save(@Valid @RequestBody CategoryDto categoryDto) {
    	Category newCreatedCategory = categoryService.save(CategoryMapper.INSTANCE.mapToEntity(categoryDto));
        CategoryDto newCreatedCategoryDto = CategoryMapper.INSTANCE.mapToDto(newCreatedCategory);
        return newCreatedCategoryDto;
    }

    @Override
    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable(name = "id") Long categoryId) {
    	categoryService.deleteById(categoryId);
    }

    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    @Override
    public Collection<CategoryDto> findAll() {
        Collection<CategoryDto> categoryDtos = CategoryMapper.INSTANCE.mapToDtos(categoryService.findAll());
        return categoryDtos;
    }

    @Override
    @GetMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto findById(@PathVariable("id") Long categoryId) {
        return CategoryMapper.INSTANCE.mapToDto(categoryService.findById(categoryId));
    }

    @Override
    @PutMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateById(@PathVariable("id") Long categoryId , @Valid @RequestBody CategoryDto updatedBody) {
        Category updatedCategory = categoryService.updateById(categoryId, CategoryMapper.INSTANCE.mapToEntity(updatedBody));
        return CategoryMapper.INSTANCE.mapToDto(updatedCategory);
    }



}
