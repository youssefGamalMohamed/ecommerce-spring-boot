package com.youssefgamal.productservice.controller.impl;

import java.util.Collection;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.youssefgamal.productservice.controller.framework.IProductController;
import com.youssefgamal.productservice.dtos.ProductDto;
import com.youssefgamal.productservice.entity.Product;
import com.youssefgamal.productservice.exception.type.IdNotFoundException;
import com.youssefgamal.productservice.mappers.ProductMapper;
import com.youssefgamal.productservice.service.framework.IProductService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ProductController implements IProductController {


    @Autowired
    private IProductService productService;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto save(@RequestBody ProductDto productDto) throws Exception {
    	log.info("save(): {}",productDto);
    	Product createdProduct = productService.save(productMapper.mapToEntity(productDto));
    	ProductDto createdProductDto = productMapper.mapToDto(createdProduct);
    	return createdProductDto;
    }

    
    @Override
    @GetMapping("/products")
    @ResponseStatus(HttpStatus.OK)
    public Collection<ProductDto> findAll() {
    	Set<Product> products = productService.findAll();
		Collection<ProductDto> productDtos = productMapper.mapToDtos(products);
		return productDtos;
    }
    
    
    
   
    @Override
    @PutMapping("/products/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductDto updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDto productDto) {
    	
    	Product updateProduct = productService.updateProductById(id, productMapper.mapToEntity(productDto));
		ProductDto updatedProductDto = productMapper.mapToDto(updateProduct);
		
		return updatedProductDto;
    }

    
    @Override
    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable(name = "id") Long productId) throws IdNotFoundException {
    	productService.deleteProductById(productId);
    }

    @Override
    @DeleteMapping("/products")
    @ResponseStatus(HttpStatus.OK)
	public void deleteByCategoryId(@RequestParam Long category_id) {
		productService.deleteAllProductsWithCategoryId(category_id);
	}

    @Override
    @GetMapping("/products/{id}")
	@ResponseStatus(HttpStatus.OK)
	public ProductDto findById(Long id) {
		Product product = productService.findById(id);
		ProductDto productDto = productMapper.mapToDto(product);
		return productDto;
	}

       
}

