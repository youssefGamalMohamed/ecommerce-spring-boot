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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ProductController implements IProductController {


    private final IProductService productService;
    private final ProductMapper productMapper;

    @PostMapping("/products")
    @Override
    public ResponseEntity<?> save(@Valid @RequestBody ProductDto productDto) {
        log.info("save({})", productDto);
    	Product newCreatedProduct = productService.save(productMapper.mapToEntity(productDto));
        return new ResponseEntity<>(
        			productMapper.mapToDto(newCreatedProduct),
        			HttpStatus.CREATED
        		);
    }

    @GetMapping("/products")
    @Override
    public ResponseEntity<?> findProductsByCategoryName(@RequestParam(value = "category") String categoryName) {
        log.info("findProductsByCategoryName({})", categoryName);
    	Set<Product> productDtos = productService.findAllByCategoryName(categoryName);
		return ResponseEntity.ok(productMapper.mapToDtos(productDtos));
    }

    @PutMapping("/products/{id}")
    @Override
    public ResponseEntity<?> updateById(@PathVariable("id") Long productId , @Valid @RequestBody ProductDto updatedBody) {
        log.info("updateById({}, {})", productId, updatedBody);
    	
    	Product updatedProduct = productService.updateById(productId, productMapper.mapToEntity(updatedBody));
		return new ResponseEntity<>(
					productMapper.mapToDto(updatedProduct),
					HttpStatus.OK
				);
    }

    @DeleteMapping("/products/{id}")
    @Override
    public ResponseEntity<?> deleteById(@PathVariable("id") Long productId) throws IdNotFoundException {
        log.info("deleteById({})", productId);
    	productService.deleteById(productId);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

}

