package com.app.ecommerce.service.impl;


import com.app.ecommerce.entity.Category;
import com.app.ecommerce.entity.Product;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.exception.type.NameNotFoundException;
import com.app.ecommerce.models.request.PostProductRequestBody;
import com.app.ecommerce.models.request.PutProductRequestBody;
import com.app.ecommerce.models.response.endpoints.AddNewProductResponse;
import com.app.ecommerce.models.response.endpoints.DeleteProductByIdResponse;
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
    private CategoryService categoryService;


    @Override
    public AddNewProductResponse addNewProduct(PostProductRequestBody productRequestBody) {

        Set<Category> categories = categoryService.getCategories(productRequestBody.getCategoriesId());

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
		Set<Product> productSet = categoryService.getAllProductsByCategoryName(categoryName);

		return GetAllProductsByCategoryNameResponse.builder()
				.products(productSet)
				.build();
	}


	@Override
	public UpdateProductResponse updateProductById(Long productId, PutProductRequestBody updatedProductRequestBody) {
			
		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new IdNotFoundException("Can Not Update Product , Id Not Found"));
		
        Set<Category> categories = categoryService.getCategories(updatedProductRequestBody.getCategoriesId());
        
        product.setName(updatedProductRequestBody.getName());
        product.setDescription(updatedProductRequestBody.getDescription());
        product.setPrice(updatedProductRequestBody.getPrice());
        product.setQuantity(updatedProductRequestBody.getQuantity());
        product.getCategories().addAll(categories);
        
        productRepo.save(product);
        
        return UpdateProductResponse.builder()
        		.id(productId)
        		.build();
	}

	@Override
	public DeleteProductByIdResponse deleteProductById(Long productId) {
		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new IdNotFoundException("Product Id Not Found to Delete"));

		for(Category category : product.getCategories())
			category.removeProduct(product);

		product.getCategories().clear();

		productRepo.deleteById(productId);


		return DeleteProductByIdResponse.builder()
				.message("Product Deleted Successfully")
				.build();
	}
}
