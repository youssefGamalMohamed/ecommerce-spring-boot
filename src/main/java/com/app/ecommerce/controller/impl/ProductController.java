package com.app.ecommerce.controller.impl;

import com.app.ecommerce.controller.framework.IProductController;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.models.request.PostProductRequestBody;
import com.app.ecommerce.models.request.PutProductRequestBody;
import com.app.ecommerce.service.framework.IProductService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/products/{id}")
    @RolesAllowed({"ADMIN"})
    @Override
    public ResponseEntity<?> deleteById(@PathVariable(name = "id") Long productId) throws IdNotFoundException {
        return new ResponseEntity<>(
                productService.deleteProductById(productId) ,
                HttpStatus.NO_CONTENT
        );
    }

}

