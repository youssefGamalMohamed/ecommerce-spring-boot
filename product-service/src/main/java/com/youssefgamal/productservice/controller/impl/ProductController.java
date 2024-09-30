package com.youssefgamal.productservice.controller.impl;

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

import com.youssefgamal.productservice.controller.framework.IProductController;
import com.youssefgamal.productservice.dtos.ProductDto;
import com.youssefgamal.productservice.entity.Product;
import com.youssefgamal.productservice.exception.type.IdNotFoundException;
import com.youssefgamal.productservice.mappers.ProductMapper;
import com.youssefgamal.productservice.service.framework.ICategoryService;
import com.youssefgamal.productservice.service.framework.IProductService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

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

    @DeleteMapping("/products")
	@Override
	public ResponseEntity<?> deleteByCategoryId(@RequestParam Long category_id) {
		productService.deleteAllProductsWithCategoryId(category_id);
		return ResponseEntity.ok().build();
	}


    @GetMapping("/products/{id}")
	@Override
	public ResponseEntity<?> findById(Long id) {
		Product product = productService.findById(id);
		return ResponseEntity.ok(ProductMapper.INSTANCE.mapToDto(product));
	}

       
}

