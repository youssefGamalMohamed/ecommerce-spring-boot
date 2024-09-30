package com.app.ecommerce.service.impl;


import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.ecommerce.entity.Category;
import com.app.ecommerce.entity.Product;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.repository.ProductRepo;
import com.app.ecommerce.service.framework.IProductService;

@Service
public class ProductService implements IProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CategoryService categoryService;


    @Override
    public Product save(Product newProduct) {

        Set<Category> categories = categoryService.getCategories(
        				newProduct.getCategories().stream()
        							.map(Category::getId).collect(Collectors.toSet())
        		);

        Product product = Product.builder()
        		.name(newProduct.getName())
        		.description(newProduct.getDescription())
        		.price(newProduct.getPrice())
        		.quantity(newProduct.getQuantity())
        		.categories(categories)
        		.build();
        
        return productRepo.save(product);
    }


	@Override
	public Set<Product> findProductsByCategoryName(String categoryName) {
		Set<Product> productSet = categoryService.getAllProductsByCategoryName(categoryName);
		return productSet;
	}


	@Override
	public Product updateProductById(Long productId, Product updatedProductData) {
			
		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new IdNotFoundException("Can Not Update Product , Id Not Found"));
		
        Set<Category> categories = categoryService.getCategories(updatedProductData.getCategories()
        		.stream().map(Category::getId).collect(Collectors.toSet()));
        
        product.setName(updatedProductData.getName());
        product.setDescription(updatedProductData.getDescription());
        product.setPrice(updatedProductData.getPrice());
        product.setQuantity(updatedProductData.getQuantity());
        product.getCategories().addAll(categories);
        
        return productRepo.save(product);
        
	}

	@Override
	public void deleteProductById(Long productId) {
		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new IdNotFoundException("Product Id Not Found to Delete"));

		for(Category category : product.getCategories())
			category.removeProduct(product);

		product.getCategories().clear();

		productRepo.deleteById(productId);
	}
}
