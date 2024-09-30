package com.youssefgamal.productservice.service.framework;

import java.util.Optional;

import com.youssefgamal.productservice.entity.Category;


public interface ICategoryService {
		
	Category save(Category category);
	
	
	Optional<Category> findById(Long id);

	void deleteAllCatgoriesByCategoryId(Long categoryId);
}
