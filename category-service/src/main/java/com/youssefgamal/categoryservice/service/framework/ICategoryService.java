package com.youssefgamal.categoryservice.service.framework;



import com.youssefgamal.categoryservice.entity.Category;
import com.youssefgamal.categoryservice.exception.type.DuplicatedUniqueColumnValueException;

import java.util.List;
import java.util.NoSuchElementException;



public interface ICategoryService {
	
    Category save(Category category) throws DuplicatedUniqueColumnValueException;

    void deleteById(Long categoryId) throws NoSuchElementException;

    List<Category> findAll();

	Category findById(Long categoryId);
	
	Category updateById(Long categoryId , Category updatedCategory);

}