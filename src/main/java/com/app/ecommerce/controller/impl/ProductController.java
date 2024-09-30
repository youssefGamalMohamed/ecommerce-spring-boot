package com.app.ecommerce.controller.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.ecommerce.controller.framework.IProductController;
import com.app.ecommerce.dtos.ProductDto;
import com.app.ecommerce.entity.Product;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.mappers.ProductMapper;
import com.app.ecommerce.service.framework.IProductService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;

@RestController
public class ProductController implements IProductController {


    @Autowired
    private IProductService productService;

    @RolesAllowed({"ADMIN"})
    @PostMapping("/products")
    @Override
    public ResponseEntity<?> save(@RequestBody ProductDto productDto) {
    	Product product = productService.save(ProductMapper.INSTANCE.mapToEntity(productDto));
        return new ResponseEntity<>(
        			ProductMapper.INSTANCE.mapToDto(product),
        			HttpStatus.CREATED
        		);
    }

    @RolesAllowed({"ADMIN" , "USER"})
    @GetMapping("/products")
    @Override
    public ResponseEntity<?> findProductsByCategoryName(@RequestParam(value = "category") String categoryName) {
    	Set<Product> productDtos = productService.findProductsByCategoryName(categoryName);
		return ResponseEntity.ok(ProductMapper.INSTANCE.mapToDtos(productDtos));
    }

    @RolesAllowed({"ADMIN"})
    @PutMapping("/products/{id}")
    @Override
    public ResponseEntity<?> updateProduct(@PathVariable(value = "id") Long productId 
    		, @Valid @RequestBody ProductDto productDto) {
    	
    	Product updateProduct = productService.updateProductById(productId, ProductMapper.INSTANCE.mapToEntity(productDto));
		return new ResponseEntity<>(
					ProductMapper.INSTANCE.mapToDto(updateProduct),
					HttpStatus.OK
				);
    }

    @DeleteMapping("/products/{id}")
    @RolesAllowed({"ADMIN"})
    @Override
    public ResponseEntity<?> deleteById(@PathVariable(name = "id") Long productId) throws IdNotFoundException {
    	productService.deleteProductById(productId);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

}

