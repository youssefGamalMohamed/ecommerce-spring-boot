package com.app.ecommerce.controller.impl;

import com.app.ecommerce.controller.framework.IProductController;
import com.app.ecommerce.dtos.ProductDto;
import com.app.ecommerce.entity.Product;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.mappers.ProductMapper;
import com.app.ecommerce.service.framework.IProductService;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class ProductController implements IProductController {


    private final IProductService productService;
    private final ProductMapper productMapper;

    @PostMapping("/products")
    @Override
    public ResponseEntity<?> save(@RequestBody ProductDto productDto) {
    	Product product = productService.save(productMapper.mapToEntity(productDto));
        return new ResponseEntity<>(
        			productMapper.mapToDto(product),
        			HttpStatus.CREATED
        		);
    }

    @GetMapping("/products")
    @Override
    public ResponseEntity<?> findProductsByCategoryName(@RequestParam(value = "category") String categoryName) {
    	Set<Product> productDtos = productService.findAllByCategoryName(categoryName);
		return ResponseEntity.ok(productMapper.mapToDtos(productDtos));
    }

    @PutMapping("/products/{id}")
    @Override
    public ResponseEntity<?> updateProduct(@PathVariable(value = "id") Long productId 
    		, @Valid @RequestBody ProductDto productDto) {
    	
    	Product updateProduct = productService.updateById(productId, productMapper.mapToEntity(productDto));
		return new ResponseEntity<>(
					productMapper.mapToDto(updateProduct),
					HttpStatus.OK
				);
    }

    @DeleteMapping("/products/{id}")
    @Override
    public ResponseEntity<?> deleteById(@PathVariable(name = "id") Long productId) throws IdNotFoundException {
    	productService.deleteById(productId);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

}

