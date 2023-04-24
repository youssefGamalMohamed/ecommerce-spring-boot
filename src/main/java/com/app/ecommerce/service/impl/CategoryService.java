package com.app.ecommerce.service.impl;


import com.app.ecommerce.entity.Category;
import com.app.ecommerce.entity.Product;
import com.app.ecommerce.exception.type.DuplicatedUniqueColumnValueException;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.CategoryRequestBody;
import com.app.ecommerce.models.response.success.AddNewCategoryResponse;
import com.app.ecommerce.models.response.success.DeleteCategoryResponse;
import com.app.ecommerce.models.response.success.GetAllCategoriesReponse;
import com.app.ecommerce.repository.CategoryRepo;
import com.app.ecommerce.repository.ProductRepo;
import com.app.ecommerce.service.framework.ICategoryService;
import com.app.ecommerce.service.framework.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    
    
    
    @Override
    public AddNewCategoryResponse add(CategoryRequestBody categoryRequestBody) throws DuplicatedUniqueColumnValueException {
    	if(categoryRepo.findByName(categoryRequestBody.getName()).isPresent())
    		throw new DuplicatedUniqueColumnValueException("Category Name Already Exist" +
    					"and Should Not Be Duplicated"
    				);
    	
        Category category = categoryRepo.save(
        			Category.builder()
        			.name(categoryRequestBody.getName())
        			.build()
        		);
          
        categoryRepo.save(category);
        
        return AddNewCategoryResponse.builder()
        		.id(category.getId())
        		.build();
    }

    @Override
    public DeleteCategoryResponse deleteById(Long categoryId) throws IdNotFoundException {
        if(!categoryRepo.existsById(categoryId))
            throw new IdNotFoundException("Cateogory Id Not Found to Delete");
        
        categoryRepo.deleteById(categoryId);
        
        return DeleteCategoryResponse.builder()
        		.message("Cateogry Deleted Successfully")
        		.build();
    }

    @Override
    public GetAllCategoriesReponse findAll() {
        return 	GetAllCategoriesReponse.builder()
        		.categories(
        				categoryRepo.findAll()
        		)
        		.build();
    }


}
