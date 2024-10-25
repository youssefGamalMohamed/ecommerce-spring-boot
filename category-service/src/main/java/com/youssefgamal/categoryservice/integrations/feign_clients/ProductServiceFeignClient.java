package com.youssefgamal.categoryservice.integrations.feign_clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-microservice", url = "http://localhost:9092/ecommerce/api/v1")
public interface ProductServiceFeignClient {
	
	@DeleteMapping("/products")
	void deleteAllProductsByCategoryId(@RequestParam(name = "category_id") Long category_id);
}
