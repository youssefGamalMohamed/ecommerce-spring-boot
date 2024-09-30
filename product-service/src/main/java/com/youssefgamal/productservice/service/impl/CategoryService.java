package com.youssefgamal.productservice.service.impl;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youssefgamal.productservice.entity.Category;
import com.youssefgamal.productservice.repository.CategoryRepo;
import com.youssefgamal.productservice.service.framework.ICategoryService;

@Service
public class CategoryService implements ICategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

	@Override
	public Category save(Category category) {
		return categoryRepo.save(category);
	}

	@Override
	public Optional<Category> findById(Long id) {
		return categoryRepo.findById(id);
	}

	@Override
	public void deleteAllCatgoriesByCategoryId(Long categoryId) {
		categoryRepo.deleteAllCategoriesByCategoryIdFromCategoryService(categoryId);
	}


}
