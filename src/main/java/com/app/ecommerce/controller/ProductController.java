package com.app.ecommerce.controller;

import com.app.ecommerce.entity.Product;
import com.app.ecommerce.models.request.ProductRequestBody;
import com.app.ecommerce.service.framework.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {


    @Autowired
    private IProductService productService;

    @PostMapping("/products")
    public ResponseEntity<?> add(@RequestBody ProductRequestBody productRequestBody) {
        return ResponseEntity.ok(productService.addNewProduct(productRequestBody));
    }
    
    @GetMapping("/products/categories/{categoryId}")
    public ResponseEntity<?> findProductsByCategoryId(@PathVariable Long categoryId) {
		return ResponseEntity.ok(productService.findProductsByCategoryId(categoryId));
    	
    }
}
