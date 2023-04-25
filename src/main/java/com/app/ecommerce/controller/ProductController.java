package com.app.ecommerce.controller;

import com.app.ecommerce.models.request.PostProductRequestBody;
import com.app.ecommerce.models.request.PutProductRequestBody;
import com.app.ecommerce.service.framework.IProductService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {


    @Autowired
    private IProductService productService;

    @PostMapping("/products")
    public ResponseEntity<?> addNewProduct(@RequestBody PostProductRequestBody productRequestBody) {
        return new ResponseEntity<>(
        			productService.addNewProduct(productRequestBody) ,
        			HttpStatus.CREATED
        		);
    }
    
    @GetMapping("/products")
    public ResponseEntity<?> findProductsByCategoryId(@RequestParam(value = "category") String categoryName) {
		return ResponseEntity.ok(productService.findProductsByCategoryName(categoryName));
    }
    
    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable(value = "id") Long productId 
    		, @Valid @RequestBody PutProductRequestBody updatedProductRequestBody) {
		return new ResponseEntity<>(
					productService.updateProductById(productId, updatedProductRequestBody) ,
					HttpStatus.OK
				);
    }
    
}

