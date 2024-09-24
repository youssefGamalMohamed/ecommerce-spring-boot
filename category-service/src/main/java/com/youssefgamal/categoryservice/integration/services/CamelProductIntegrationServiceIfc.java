package com.youssefgamal.categoryservice.integration.services;

import org.springframework.http.HttpStatus;


import com.youssefgamal.categoryservice.dtos.ProductDto;

public interface CamelProductIntegrationServiceIfc {
	
	
	HttpStatus deleteById(Long id);
	
	ProductDto[] findAll();
	
	ProductDto[] findAllByCategoryName(String categoryName);
	
	HttpStatus deleteCategoryFromProduct(Long categoryId);
}
