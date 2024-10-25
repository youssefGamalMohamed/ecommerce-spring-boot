package com.youssefgamal.categoryservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youssefgamal.categoryservice.integrations.feign_clients.ProductServiceFeignClient;
import com.youssefgamal.categoryservice.service.framework.IProductService;

@Service
public class ProductServiceFeignClientImpl implements IProductService {

	@Autowired
	private ProductServiceFeignClient productServiceFeignClient;
	
	@Override
	public void deleteAllProductsByCategoryId(Long categoryId) {
		productServiceFeignClient.deleteAllProductsByCategoryId(categoryId);
	}
	
	
}
