package com.app.ecommerce.service.impl;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.ecommerce.entity.Category;
import com.app.ecommerce.entity.Product;
import com.app.ecommerce.exception.type.DuplicatedUniqueColumnValueException;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.exception.type.NameNotFoundException;
import com.app.ecommerce.repository.CategoryRepo;
import com.app.ecommerce.service.framework.ICategoryService;

@Service
public class CategoryService implements ICategoryService {

    @Autowired
    private CategoryRepo categoryRepo;


    @Override
    public Category save(Category category) throws DuplicatedUniqueColumnValueException {
    	if(categoryRepo.findByName(category.getName()).isPresent())
    		throw new DuplicatedUniqueColumnValueException("Category Name Already Exist" +
    					"and Should Not Be Duplicated"
    				);
    	
        return categoryRepo.save(
        			Category.builder()
        			.name(category.getName())
        			.build()
        		);
    }

    @Override
    public void deleteById(Long categoryId) throws IdNotFoundException {
    	
        Category category = categoryRepo.findById(categoryId).orElseThrow(
        				() -> new IdNotFoundException("Category Id Not Exist to Delete")
        		);

		for (Product product : category.getProducts()) {
			product.removeCategory(category);
		}
        

        categoryRepo.deleteById(categoryId);
    }

    @Override
    public List<Category> findAll() {
    	return categoryRepo.findAll();
    }

	@Override
	public Category findById(Long categoryId) {
		return categoryRepo.findById(categoryId)
				.orElseThrow(() -> new IdNotFoundException("No Category To Retrieve, Id Not Found"));
	}

	@Override
	public Category updateById(Long categoryId, Category updatedCategory) {
		
		Category category = categoryRepo.findById(categoryId).orElseThrow(() -> new IdNotFoundException("No Category Update, Id Not Found"));
		
		category.setName(updatedCategory.getName());
		
		return categoryRepo.save(category);
	}

	@Override
	public Set<Category> getCategories(Set<Long> categoriesIds) {

		return categoriesIds.stream()
				.map(this::getCategory)
				.collect(Collectors.toSet());

	}

	@Override
	public Category getCategory(Long id) {
		return categoryRepo.findById(id)
				.orElseThrow(
						() -> new IdNotFoundException("Can Not Create New Product"
									+ " , Some of Assigned Categories with Id " + id)
				);
	}

	@Override
	public Set<Product> getAllProductsByCategoryName(String categoryName) {
		return categoryRepo.findByName(categoryName)
				.orElseThrow(() -> new NameNotFoundException("Category Name does not exist to retrieve associated Products"))
				.getProducts();
	}


}
