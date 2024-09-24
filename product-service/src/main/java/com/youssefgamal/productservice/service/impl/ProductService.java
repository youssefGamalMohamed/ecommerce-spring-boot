package com.youssefgamal.productservice.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youssefgamal.productservice.dtos.CategoryDto;
import com.youssefgamal.productservice.entity.Category;
import com.youssefgamal.productservice.entity.Product;
import com.youssefgamal.productservice.exception.type.IdNotFoundException;
import com.youssefgamal.productservice.integration.services.CategoryIntegrationServiceIfc;
import com.youssefgamal.productservice.mappers.CategoryMapper;
import com.youssefgamal.productservice.repository.ProductRepo;
import com.youssefgamal.productservice.service.framework.IProductService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ProductService implements IProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryIntegrationServiceIfc categoryIntegrationServiceIfc;
    
    @Override
    public Product save(Product newProduct) throws Exception {
        log.info("Saving product: {}", newProduct);

        // Validate and fetch categories using integration service
        Set<CategoryDto> validCategories = newProduct.getCategories()
                .stream()
                .map(Category::getId)
                .map(this::fetchCategoryById)
                .collect(Collectors.toSet());

        // Map to entities and save each category
        Product product = Product.builder()
                .name(newProduct.getName())
                .description(newProduct.getDescription())
                .price(newProduct.getPrice())
                .quantity(newProduct.getQuantity())
                .categories(new HashSet<>())
                .build();
        
        
        Set<Category> categories = validCategories.stream()
                .map(CategoryMapper.INSTANCE::mapToEntity)
                .peek(category -> category.setProduct(product))
                .peek(category -> product.getCategories().add(category))
                .collect(Collectors.toSet());

        
        return productRepo.save(product);  // Save product with associated categories
     }

    
    // Fetch category by ID from the integration service or throw an exception
    private CategoryDto fetchCategoryById(Long categoryId) {
        CategoryDto categoryDto = categoryIntegrationServiceIfc.findById(categoryId);
        if (categoryDto == null || categoryDto.getId() == null) {
            throw new NoSuchElementException("Category not found with ID = " + categoryId);
        }
        return categoryDto;
    }

	@Override
	public Set<Product> findProductsByCategoryName(String categoryName) {
		Set<Category> categories = categoryService.findCategoriesByNameIgnoreCase(categoryName);
		Set<Product> products = categories.stream()
				.map(Category::getProduct)
				.collect(Collectors.toSet());
		
		return products;
	}


	@Override
	public Product updateProductById(Product updatedProduct) {
			
		Product product = productRepo.findById(updatedProduct.getId())
				.orElseThrow(() -> new IdNotFoundException("Can Not Update Product , Id Not Found"));
		
		Set<Category> categories = updatedProduct.getCategories()
				.stream()
				.map(category -> {
					Optional<Category> optionalCategory = categoryService.findById(category.getId());
					if(optionalCategory.isEmpty())
						return CategoryMapper.INSTANCE.mapToEntity(fetchCategoryById(category.getId()));
					return optionalCategory.get();
				})
				.peek(category -> category.setProduct(product))
				.peek(category -> product.getCategories().add(category))
				.collect(Collectors.toSet());
		
        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setQuantity(updatedProduct.getQuantity());
        
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
	public void deleteCategoryFromProduct(Long productId, Long categoryId) {
		Product product = productRepo.findById(productId)
				.orElseThrow();
		
		Category category = product.getCategories()
				.stream()
				.filter(o -> o.getId().equals(categoryId))
				.findFirst()
				.orElseThrow();
		
		
		product.getCategories().remove(category);
		
		productRepo.save(product);
		
	}
}
