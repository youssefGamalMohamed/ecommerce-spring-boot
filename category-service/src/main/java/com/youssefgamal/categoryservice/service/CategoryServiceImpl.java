package com.youssefgamal.categoryservice.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youssefgamal.categoryservice.entity.Category;
import com.youssefgamal.categoryservice.exception.DuplicatedUniqueColumnValueException;
import com.youssefgamal.categoryservice.repository.CategoryRepo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryServiceIfc {

    @Autowired
    private CategoryRepo categoryRepo;
    
    
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
    	log.info("starting deleteById({})", categoryId);
        Category category = categoryRepo.findById(categoryId).orElseThrow(
        				() -> new NoSuchElementException("Category Id Not Exist to Delete")
        		);        
        categoryRepo.deleteById(categoryId);
        log.info("end deleteById({})", categoryId);
    }

    @Override
    public List<Category> findAll() {
    	log.info("start findAll()");
    	List<Category> categories = categoryRepo.findAll();
    	log.info("start findAll(): " + categories);
    	return categories;
    }

	@Override
	public Category findById(Long categoryId) {
		log.info("start findById({})", categoryId);
		Category category =  categoryRepo.findById(categoryId)
				.orElseThrow(() -> new NoSuchElementException("No Category To Retrieve, Id Not Found"));
		log.info("end findById({}): " + category);
		return category;
	}

	@Override
	public Category updateById(Long categoryId, Category updatedCategory) {
		log.info("start updateById(id: {}, updatedCategory: {})", categoryId, updatedCategory);
		Category category = categoryRepo.findById(categoryId).orElseThrow(() -> new NoSuchElementException("No Category Update, Id Not Found"));
		
		category.setName(updatedCategory.getName());
		
		category = categoryRepo.save(category);
		log.info("end updateById(id: {}, updatedCategory: {})", categoryId, updatedCategory);
		return category;
	}
}
