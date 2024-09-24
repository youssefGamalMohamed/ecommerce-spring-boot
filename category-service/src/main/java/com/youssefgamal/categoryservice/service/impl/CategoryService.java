package com.youssefgamal.categoryservice.service.impl;


import com.youssefgamal.categoryservice.dtos.ProductDto;
import com.youssefgamal.categoryservice.entity.Category;
import com.youssefgamal.categoryservice.exception.type.DuplicatedUniqueColumnValueException;
import com.youssefgamal.categoryservice.integration.services.CamelProductIntegrationServiceIfc;
import com.youssefgamal.categoryservice.repository.CategoryRepo;
import com.youssefgamal.categoryservice.service.framework.ICategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class CategoryService implements ICategoryService {

    @Autowired
    private CategoryRepo categoryRepo;


    @Autowired
    private CamelProductIntegrationServiceIfc camelProductIntegrationServiceIfc;
    
    @Override
    public Category save(Category category) throws DuplicatedUniqueColumnValueException {
		log.info("starting save({})",category);
    	if(categoryRepo.findByName(category.getName()).isPresent())
    		throw new DuplicatedUniqueColumnValueException("Category Name Already Exist" +
    					"and Should Not Be Duplicated");

        Category newSaved = categoryRepo.save(category);
		log.info("end save({})",newSaved);
		return newSaved;
    }

    @Override
    public void deleteById(Long categoryId) throws NoSuchElementException {
    	
        Category category = categoryRepo.findById(categoryId).orElseThrow(
        				() -> new NoSuchElementException("Category Id Not Exist to Delete")
        		);
        
        HttpStatus responseCode =  camelProductIntegrationServiceIfc.deleteCategoryFromProduct(category.getId());
                
        log.info("deleteById() , response-code = " , responseCode);
        categoryRepo.deleteById(categoryId);
        
    }

    @Override
    public List<Category> findAll() {
    	return categoryRepo.findAll();
    }

	@Override
	public Category findById(Long categoryId) {
		return categoryRepo.findById(categoryId)
				.orElseThrow(() -> new NoSuchElementException("No Category To Retrieve, Id Not Found"));
	}

	@Override
	public Category updateById(Long categoryId, Category updatedCategory) {
		
		Category category = categoryRepo.findById(categoryId).orElseThrow(() -> new NoSuchElementException("No Category Update, Id Not Found"));
		
		category.setName(updatedCategory.getName());
		
		return categoryRepo.save(category);
	}
}
