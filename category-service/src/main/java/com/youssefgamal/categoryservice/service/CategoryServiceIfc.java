package com.youssefgamal.categoryservice.service;



import java.util.List;
import java.util.NoSuchElementException;

import com.youssefgamal.categoryservice.entity.Category;
import com.youssefgamal.categoryservice.exception.DuplicatedUniqueColumnValueException;



public interface CategoryServiceIfc {
	
    Category save(Category category) throws DuplicatedUniqueColumnValueException;

    void deleteById(Long categoryId) throws NoSuchElementException;

    List<Category> findAll();

	Category findById(Long categoryId);
	
	Category updateById(Long categoryId , Category updatedCategory);

}
