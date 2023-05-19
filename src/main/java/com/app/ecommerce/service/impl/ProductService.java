package com.app.ecommerce.service.impl;


import com.app.ecommerce.entity.Category;
import com.app.ecommerce.entity.Product;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.exception.type.NameNotFoundException;
import com.app.ecommerce.models.request.PostProductRequestBody;
import com.app.ecommerce.models.request.PutProductRequestBody;
import com.app.ecommerce.models.response.endpoints.AddNewProductResponse;
import com.app.ecommerce.models.response.endpoints.GetAllProductsByCategoryNameResponse;
import com.app.ecommerce.models.response.endpoints.UpdateProductResponse;
import com.app.ecommerce.repository.CategoryRepo;
import com.app.ecommerce.repository.ProductRepo;
import com.app.ecommerce.service.framework.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;
@Service
public class ProductService implements IProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CategoryRepo categoryRepo;


    @Override
    public AddNewProductResponse addNewProduct(PostProductRequestBody productRequestBody) {

        Set<Category> categories = new HashSet<>();
        productRequestBody.getCategoriesId()
                .forEach(id -> {
                	Category category = categoryRepo.findById(id)
                			.orElseThrow(() -> new IdNotFoundException("Can Not Create New Product"
                					+ " , Some of Assigned Categories with Id " + id));
                	categories.add(category);
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
	public GetAllProductsByCategoryNameResponse findProductsByCategoryName(String categoryName) {
		Set<Product> productSet = categoryRepo.findByName(categoryName)
				.orElseThrow(() -> new NameNotFoundException("Category Name does not exist to retrieve associated Products"))
				.getProducts();
		return GetAllProductsByCategoryNameResponse.builder()
				.products(
					productSet
				)
				.build();
	}


	@Override
	public UpdateProductResponse updateProductById(Long productId, PutProductRequestBody updatedProductRequstBody) {
			
		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new IdNotFoundException("Can Not Update Product , Id Not Found"));
		
        Set<Category> categories = new HashSet<>();
        
        updatedProductRequstBody.getCategoriesId()
                .forEach(id -> {
                	Category category = categoryRepo.findById(id)
                			.orElseThrow(() -> new IdNotFoundException("Can Not Update Product , Category with id = " + 
                							id + "Not Eixst"
                					));
                	categories.add(category);
                });
        
        product.setName(updatedProductRequstBody.getName());
        product.setDescription(updatedProductRequstBody.getDescription());
        product.setPrice(updatedProductRequstBody.getPrice());
        product.setQuantity(updatedProductRequstBody.getQuantity());
        product.getCategories().addAll(categories);
        
        productRepo.save(product);
        
        return UpdateProductResponse.builder()
        		.id(productId)
        		.build();
	}
}
