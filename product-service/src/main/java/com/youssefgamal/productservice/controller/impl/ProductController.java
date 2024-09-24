package com.youssefgamal.productservice.controller.impl;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.youssefgamal.productservice.controller.framework.IProductController;
import com.youssefgamal.productservice.dtos.ProductDto;
import com.youssefgamal.productservice.entity.Product;
import com.youssefgamal.productservice.exception.type.IdNotFoundException;
import com.youssefgamal.productservice.mappers.ProductMapper;
import com.youssefgamal.productservice.service.framework.ICategoryService;
import com.youssefgamal.productservice.service.framework.IProductService;

@RestController
@Slf4j
public class ProductController implements IProductController {


    @Autowired
    private IProductService productService;
    
    @Autowired
    private ICategoryService categoryService;

    @RolesAllowed({"ADMIN"})
    @PostMapping("/products")
    @Override
    public ResponseEntity<?> save(@RequestBody ProductDto productDto) throws Exception {
    	log.info("save(): {}",productDto);
    	Product product = productService.save(ProductMapper.INSTANCE.mapToEntity(productDto));
        return new ResponseEntity<>(
        			ProductMapper.INSTANCE.mapToDto(product),
        			HttpStatus.CREATED
        		);
    }

    @RolesAllowed({"ADMIN" , "USER"})
    @GetMapping("/products/{categoryName}")
    @Override
    public ResponseEntity<?> findProductsByCategoryName(@RequestParam(value = "categoryName") String categoryName) {
    	Set<Product> productDtos = productService.findProductsByCategoryName(categoryName);
		return ResponseEntity.ok(ProductMapper.INSTANCE.mapToDtos(productDtos));
    }

    
    @RolesAllowed({"ADMIN" , "USER"})
    @GetMapping("/products")
    @Override
    public ResponseEntity<?> findAll() {
    	Set<Product> productDtos = productService.findAll();
		return ResponseEntity.ok(ProductMapper.INSTANCE.mapToDtos(productDtos));
    }
    
    
    @RolesAllowed({"ADMIN"})
    @PutMapping("/products")
    @Override
    public ResponseEntity<?> updateProduct(@Valid @RequestBody ProductDto productDto) {
    	
    	Product updateProduct = productService.updateProductById(ProductMapper.INSTANCE.mapToEntity(productDto));
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

    

    @DeleteMapping("/products/{productId}/categories/{categoryId}")
	@Override
	public ResponseEntity<?> deleteCategoryFromProduct(@PathVariable Long productId, @PathVariable Long categoryId) {
    	productService.deleteCategoryFromProduct(productId,categoryId);
		return ResponseEntity.noContent()
				.build();
	}

    @DeleteMapping("/products")
	@Override
	public ResponseEntity<?> deleteCategory(@RequestParam(name = "categoryId") Long categoryId) {
    	log.info("deleteCategory({})", categoryId);
    	categoryService.deleteAllCatgoriesByCategoryId(categoryId);
		return ResponseEntity.ok().build();
	}

    
}

