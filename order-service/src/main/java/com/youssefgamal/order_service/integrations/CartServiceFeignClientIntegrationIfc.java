package com.youssefgamal.order_service.integrations;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.youssefgamal.order_service.dtos.CartDto;


@FeignClient(name = "cart-service", url = "http://localhost:9093/ecommerce/api/v1")
public interface CartServiceFeignClientIntegrationIfc extends CartServiceIfc {
    

    @GetMapping("/carts/{id}")
    CartDto findById(@PathVariable Long id);
}
