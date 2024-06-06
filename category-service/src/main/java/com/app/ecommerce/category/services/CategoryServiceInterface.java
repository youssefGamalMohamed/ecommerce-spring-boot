package com.app.ecommerce.category.services;

import com.app.ecommerce.category.entities.Category;
import com.app.ecommerce.category.entities.Product;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public interface CategoryServiceInterface {

    Category save(Category category) throws NoSuchElementException;

    void deleteById(Long categoryId) throws NoSuchElementException;

    List<Category> findAll();

    Category findById(Long categoryId);

    Category updateById(Long categoryId , Category updatedCategory);

}
