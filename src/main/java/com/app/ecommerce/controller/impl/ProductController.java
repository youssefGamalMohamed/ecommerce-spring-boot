package com.app.ecommerce.controller.impl;

import com.app.ecommerce.controller.framework.IProductController;
import com.app.ecommerce.models.request.PostProductRequestBody;
import com.app.ecommerce.models.request.PutProductRequestBody;
import com.app.ecommerce.service.framework.IProductService;

import jakarta.annotation.security.RolesAllowed;
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
public class ProductController implements IProductController {


    @Autowired
    private IProductService productService;

    @RolesAllowed({"ADMIN"})
    @PostMapping("/products")
    @Override
    public ResponseEntity<?> addNewProduct(@Valid @RequestBody PostProductRequestBody productRequestBody) {
        return new ResponseEntity<>(
        			productService.addNewProduct(productRequestBody) ,
        			HttpStatus.CREATED
        		);
    }

    @RolesAllowed({"ADMIN" , "USER"})
    @GetMapping("/products")
    @Override
    public ResponseEntity<?> findProductsByCategoryName(@RequestParam(value = "category") String categoryName) {
		return ResponseEntity.ok(productService.findProductsByCategoryName(categoryName));
    }

    @RolesAllowed({"ADMIN"})
    @PutMapping("/products/{id}")
    @Override
    public ResponseEntity<?> updateProduct(@PathVariable(value = "id") Long productId 
    		, @Valid @RequestBody PutProductRequestBody updatedProductRequestBody) {
		return new ResponseEntity<>(
					productService.updateProductById(productId, updatedProductRequestBody) ,
					HttpStatus.OK
				);
    }
    
}

