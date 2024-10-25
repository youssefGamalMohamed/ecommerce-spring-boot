package com.youssefgamal.cart_service.integrations;

import com.youssefgamal.cart_service.dtos.ProductDto;

public interface ProductServiceIfc {
	ProductDto findById(Long id);
}
