package com.app.ecommerce.service.impl;


import com.app.ecommerce.entity.Category;
import com.app.ecommerce.entity.Product;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.ProductRequestBody;
import com.app.ecommerce.models.response.success.AddNewProductResponse;
import com.app.ecommerce.models.response.success.GetAllCategoriesReponse;
import com.app.ecommerce.models.response.success.GetAllProductsByCategoryIdResponse;
import com.app.ecommerce.repository.CategoryRepo;
import com.app.ecommerce.repository.ProductRepo;
import com.app.ecommerce.service.framework.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService implements IProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CategoryRepo categoryRepo;


    @Override
    public AddNewProductResponse addNewProduct(ProductRequestBody productRequestBody) {

        Set<Category> categories = new HashSet<>();
        productRequestBody.getCategoriesId()
                .forEach(id -> {
                	Optional<Category> category = categoryRepo.findById(id);
                	if(category.isPresent())
                		categories.add(category.get());
                	else
                		throw new IdNotFoundException("Can Not Add New Product with Category Id = " + id 
                				+ " , this Id not exist and not belong for Category");
                });

        Product product = Product.builder()
        		.name(productRequestBody.getName())
        		.description(productRequestBody.getDescription())
        		.price(productRequestBody.getPrice())
        		.quantity(productRequestBody.getQuantity())
        		.categories(categories)
        		.build();
        
        return AddNewProductResponse.builder()
        		.id(
        				productRepo.save(product).getId()
        		)
        		.build();
      
    }


	@Override
	public GetAllProductsByCategoryIdResponse findProductsByCategoryId(Long categoryId) {
		return GetAllProductsByCategoryIdResponse.builder()
				.products(
						productRepo.findByCategoriesId(categoryId)
				)
				.build();
	}
}
