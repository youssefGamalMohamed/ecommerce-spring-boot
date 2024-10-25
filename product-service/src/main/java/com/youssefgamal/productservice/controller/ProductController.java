package com.youssefgamal.productservice.controller;

import java.util.Collection;
import java.util.Set;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.youssefgamal.productservice.dtos.ProductInput;
import com.youssefgamal.productservice.entity.Product;
import com.youssefgamal.productservice.mappers.ProductMapper;
import com.youssefgamal.productservice.service.framework.ProductServiceIfc;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ProductController {

    private final ProductServiceIfc productService;
    private final ProductMapper productMapper;

    @MutationMapping
    public ProductInput save(@Argument(name = "product") @Valid ProductInput productInput) throws Exception {
        log.info("Attempting to save a new product: {}", productInput);
        Product createdProduct = productService.save(productMapper.mapToEntity(productInput));
        ProductInput createdProductInput = productMapper.mapToInput(createdProduct);
        log.info("Product successfully saved with ID: {}", createdProductInput.getId());
        return createdProductInput;
    }

    @QueryMapping
    public Collection<ProductInput> findAll() {
        log.info("Fetching all products");
        Set<Product> products = productService.findAll();
        Collection<ProductInput> productInputs = productMapper.mapToInputs(products);
        log.info("Total products found: {}", productInputs.size());
        return productInputs;
    }

    @MutationMapping
    public ProductInput updateProduct(@Argument Long id, @Argument(name = "product") @Valid ProductInput productInput) {
        log.info("Updating product with ID: {} with new data: {}", id, productInput);
        Product updatedProduct = productService.updateProductById(id, productMapper.mapToEntity(productInput));
        ProductInput updatedProductInput = productMapper.mapToInput(updatedProduct);
        log.info("Product with ID: {} successfully updated", id);
        return updatedProductInput;
    }

    @MutationMapping
    public boolean deleteById(@Argument Long id) {
        log.info("Attempting to delete product with ID: {}", id);
        productService.deleteProductById(id);
        log.info("Product with ID: {} successfully deleted", id);
        return true;
    }

    @MutationMapping
    public boolean deleteByCategoryId(@Argument Long categoryId) {
        log.info("Deleting all products associated with category ID: {}", categoryId);
        productService.deleteAllProductsWithCategoryId(categoryId);
        log.info("All products associated with category ID: {} have been deleted", categoryId);
        return true;
    }

    @QueryMapping
    public ProductInput findById(@Argument Long id) {
        log.info("Fetching product with ID: {}", id);
        Product product = productService.findById(id);
        ProductInput productInput = productMapper.mapToInput(product);
        log.info("Product retrieved with ID: {}", id);
        return productInput;
    }
}

