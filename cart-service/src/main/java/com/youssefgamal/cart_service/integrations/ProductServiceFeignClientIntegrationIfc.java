package com.youssefgamal.cart_service.integrations;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.youssefgamal.cart_service.dtos.ProductDto;

@FeignClient(name = "product-service", url = "http://localhost:9092/ecommerce/api/v1")
public interface ProductServiceFeignClientIntegrationIfc extends ProductServiceIfc {
    

    @GetMapping("/products/{id}")
    ProductDto findById(@PathVariable Long id);
}
