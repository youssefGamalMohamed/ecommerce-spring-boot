package com.youssefgamal.productservice.service.impl;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youssefgamal.productservice.entity.Category;
import com.youssefgamal.productservice.repository.CategoryRepo;
import com.youssefgamal.productservice.service.framework.CategoryServiceIfc;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryServiceIfc {

    @Autowired
    private CategoryRepo categoryRepo;

    @Override
    public Category save(Category category) {
        log.info("Attempting to save a new category: {}", category);
        Category savedCategory = categoryRepo.save(category);
        log.info("Category successfully saved with ID: {}", savedCategory.getId());
        return savedCategory;
    }

    @Override
    public Optional<Category> findById(Long id) {
        log.info("Searching for category with ID: {}", id);
        Category category = categoryRepo.findById(id)
        		.orElseThrow();
        log.info("Searching for category with ID: {} and Category = {}", id, category);
        return Optional.of(category);
    }

    @Override
    public void deleteAllCatgoriesByCategoryId(Long categoryId) {
        log.info("Deleting all categories associated with category ID: {}", categoryId);
        categoryRepo.deleteAllCategoriesByCategoryIdFromCategoryService(categoryId);
        log.info("All categories associated with category ID: {} have been successfully deleted", categoryId);
    }
}