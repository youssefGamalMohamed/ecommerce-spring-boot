package com.app.ecommerce.service.framework;


import java.util.List;
import java.util.Set;

import com.app.ecommerce.entity.Category;
import com.app.ecommerce.entity.Product;
import com.app.ecommerce.exception.type.DuplicatedUniqueColumnValueException;
import com.app.ecommerce.exception.type.IdNotFoundException;


public interface ICategoryService {
	
    Category save(Category category) throws DuplicatedUniqueColumnValueException;

    void deleteById(Long categoryId) throws IdNotFoundException;

    List<Category> findAll();

	Category findById(Long categoryId);
	
	Category updateById(Long categoryId , Category updatedCategory);

    Set<Category> getCategories(Set<Long> categoriesIds);

    Category getCategory(Long id);


    Set<Product> getAllProductsByCategoryName(String categoryName);
}
