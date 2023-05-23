package com.app.ecommerce.service.impl;


import com.app.ecommerce.entity.Category;
import com.app.ecommerce.entity.Product;
import com.app.ecommerce.exception.type.DuplicatedUniqueColumnValueException;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.PostCategoryRequestBody;
import com.app.ecommerce.models.request.PutCategoryRequestBody;
import com.app.ecommerce.models.response.endpoints.AddNewCategoryResponse;
import com.app.ecommerce.models.response.endpoints.DeleteCategoryResponse;
import com.app.ecommerce.models.response.endpoints.GetAllCategoriesResponse;
import com.app.ecommerce.models.response.endpoints.GetCategoryByIdResponse;
import com.app.ecommerce.models.response.endpoints.UpdateCategoryResponse;
import com.app.ecommerce.repository.CategoryRepo;
import com.app.ecommerce.service.framework.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Iterator;

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
    	
        Category category = categoryRepo.findById(categoryId).orElseThrow(
        				() -> new IdNotFoundException("Category Id Not Exist to Delete")
        		);
        
        Iterator<Product> iterator = category.getProducts().iterator();
        while (iterator.hasNext()) {
            Product product = iterator.next();
            product.getCategories().remove(category);
        }
        
        category.getProducts().clear();
        categoryRepo.deleteById(categoryId);
        
        
        return DeleteCategoryResponse.builder()
        		.message("Category Deleted Successfully")
        		.build();
    }

    @Override
    public GetAllCategoriesResponse findAll() {
        return 	GetAllCategoriesResponse.builder()
        		.categories(
        				categoryRepo.findAll()
        		)
        		.build();
    }

	@Override
	public GetCategoryByIdResponse findById(Long categoryId) {
		return GetCategoryByIdResponse.builder()
				.category(
					categoryRepo.findById(categoryId).orElseThrow(() -> new IdNotFoundException("No Category To Retrieve, Id Not Found"))
				 )
				.build();
	}

	@Override
	public UpdateCategoryResponse updateById(Long categoryId, PutCategoryRequestBody updatedCategory) {
		
		Category category = categoryRepo.findById(categoryId).orElseThrow(() -> new IdNotFoundException("No Category Update, Id Not Found"));
		
		category.setName(updatedCategory.getName());
		
		categoryRepo.save(category);
		
		return UpdateCategoryResponse.builder()
				.id(categoryId)
				.build();
	}


}
