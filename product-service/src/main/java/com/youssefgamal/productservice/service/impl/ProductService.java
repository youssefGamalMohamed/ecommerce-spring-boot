package com.youssefgamal.productservice.service.impl;


import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youssefgamal.productservice.entity.Category;
import com.youssefgamal.productservice.entity.Product;
import com.youssefgamal.productservice.exception.type.IdNotFoundException;
import com.youssefgamal.productservice.repository.ProductRepo;
import com.youssefgamal.productservice.service.framework.IProductService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class ProductService implements IProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CategoryService categoryService;

   
    @Override
    public Product save(Product newProduct) throws Exception {
        log.info("Saving product: {}", newProduct);


        // Map to entities and save each category
        Product product = Product.builder()
                .name(newProduct.getName())
                .description(newProduct.getDescription())
                .price(newProduct.getPrice())
                .quantity(newProduct.getQuantity())
                .categories(new HashSet<>())
                .build();
        
        // Assigning Categories to Product
        Set<Category> categories = newProduct.getCategories().stream()
                .peek(category -> category.setProduct(product))
                .peek(category -> product.getCategories().add(category))
                .collect(Collectors.toSet());

        
        return productRepo.save(product);  // Save product with associated categories
     }


	@Override
	public Product updateProductById(Product updatedProduct) {
			
		Product product = productRepo.findById(updatedProduct.getId())
				.orElseThrow(() -> new IdNotFoundException("Can Not Update Product , Id Not Found"));
		
		Set<Category> categories = updatedProduct.getCategories()
				.stream()
				.peek(category -> category.setProduct(product))
				.peek(category -> product.getCategories().add(category))
				.collect(Collectors.toSet());
		
        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setQuantity(updatedProduct.getQuantity());
        product.getCategories().addAll(categories);
        
        
        return productRepo.save(product);
	}

	@Override
	public void deleteProductById(Long productId) {
		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new IdNotFoundException("Product Id Not Found to Delete"));

		productRepo.deleteById(productId);
	}


	@Override
	public Set<Product> findAll() {
		return productRepo.findAll()
				.stream()
				.collect(Collectors.toSet());
	}


	@Override
	public void deleteAllProductsWithCategoryId(Long category_id) {
		categoryService.deleteAllCatgoriesByCategoryId(category_id);
	}


	@Override
	public Product findById(Long id) {
		return productRepo.findById(id)
				.orElseThrow();
	}

}
