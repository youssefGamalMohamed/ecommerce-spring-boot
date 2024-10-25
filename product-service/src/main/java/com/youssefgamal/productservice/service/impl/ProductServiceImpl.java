package com.youssefgamal.productservice.service.impl;


import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youssefgamal.productservice.entity.Category;
import com.youssefgamal.productservice.entity.Product;
import com.youssefgamal.productservice.repository.ProductRepo;
import com.youssefgamal.productservice.service.framework.ProductServiceIfc;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductServiceIfc {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CategoryServiceImpl categoryService;

    @Override
    public Product save(Product newProduct) throws Exception {
        log.info("Attempting to save product: {}", newProduct);

        // Prepare a new Product entity
        Product product = Product.builder()
                .name(newProduct.getName())
                .description(newProduct.getDescription())
                .price(newProduct.getPrice())
                .quantity(newProduct.getQuantity())
                .categories(new HashSet<>())
                .build();

        // Map categories to the product and set associations
        Set<Category> categories = newProduct.getCategories().stream()
                .peek(category -> {
                    category.setProduct(product);
                    product.getCategories().add(category);
                })
                .collect(Collectors.toSet());

        log.info("Categories mapped and assigned to product: {}", categories);

        Product savedProduct = productRepo.save(product);
        log.info("Product successfully saved with ID: {}", savedProduct.getId());
        return savedProduct;
    }

    @Override
    public Product updateProductById(Long id, Product updatedProduct) {
        log.info("Attempting to update product with ID: {}", id);

        Product product = productRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cannot update product - ID not found: " + id));

        log.info("Current product details: {}", product);

        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setQuantity(updatedProduct.getQuantity());

        Product savedProduct = productRepo.save(product);
        log.info("Product updated successfully: {}", savedProduct);
        return savedProduct;
    }

    @Override
    public void deleteProductById(Long productId) {
        log.info("Attempting to delete product with ID: {}", productId);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product ID not found for deletion: " + productId));

        productRepo.deleteById(productId);
        log.info("Product deleted successfully with ID: {}", productId);
    }

    @Override
    public Set<Product> findAll() {
        log.info("Fetching all products");

        Set<Product> products = productRepo.findAll()
                .stream()
                .collect(Collectors.toSet());

        log.info("Found {} products", products.size());
        return products;
    }

    @Override
    public void deleteAllProductsWithCategoryId(Long categoryId) {
        log.info("Deleting all products associated with category ID: {}", categoryId);

        categoryService.deleteAllCatgoriesByCategoryId(categoryId);

        log.info("Successfully deleted products associated with category ID: {}", categoryId);
    }

    @Override
    public Product findById(Long id) {
        log.info("Fetching product with ID: {}", id);

        Product product = productRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product ID not found: " + id));

        log.info("Found product: {}", product);
        return product;
    }
}

