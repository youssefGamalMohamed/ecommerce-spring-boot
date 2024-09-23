package com.youssefgamal.productservice.integration.services;

import com.youssefgamal.productservice.dtos.CategoryDto;

public interface CategoryIntegrationServiceIfc {
	
	CategoryDto findById(Long id);
}
