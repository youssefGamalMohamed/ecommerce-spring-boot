package com.youssefgamal.productservice.service.framework;

import java.util.Optional;
import java.util.Set;

import com.youssefgamal.productservice.entity.Category;


public interface ICategoryService {
		
	Category save(Category category);
	
	Set<Category> findCategoriesByNameIgnoreCase(String name);
	
	Optional<Category> findById(Long id);

	void deleteAllCatgoriesByCategoryId(Long categoryId);
}
