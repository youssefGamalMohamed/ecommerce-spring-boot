package com.app.ecommerce.service.impl;


import com.app.ecommerce.entity.Category;
import com.app.ecommerce.entity.Product;
import com.app.ecommerce.exception.type.DuplicatedUniqueColumnValueException;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.PostCategoryRequestBody;
import com.app.ecommerce.models.response.success.AddNewCategoryResponse;
import com.app.ecommerce.models.response.success.DeleteCategoryResponse;
import com.app.ecommerce.models.response.success.GetAllCategoriesReponse;
import com.app.ecommerce.models.response.success.GetCategoryByIdResponse;
import com.app.ecommerce.repository.CategoryRepo;
import com.app.ecommerce.repository.ProductRepo;
import com.app.ecommerce.service.framework.ICategoryService;
import com.app.ecommerce.service.framework.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

        
    @Override
    public AddNewCategoryResponse add(PostCategoryRequestBody categoryRequestBody) throws DuplicatedUniqueColumnValueException {
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
        

        
        Category category = categoryRepo.findById(categoryId).get();
        
        Iterator<Product> iterator = category.getProducts().iterator();
        while (iterator.hasNext()) {
            Product product = iterator.next();
            product.getCategories().remove(category);
        }
        
        category.getProducts().clear();
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

	@Override
	public GetCategoryByIdResponse findById(Long categoryId) {
		if(!categoryRepo.existsById(categoryId))
			throw new IdNotFoundException("Can Not Get Category By Id , Id Not Found");
		
		return GetCategoryByIdResponse.builder()
				.category(
					categoryRepo.findById(categoryId).get()
				 )
				.build();
	}


}
