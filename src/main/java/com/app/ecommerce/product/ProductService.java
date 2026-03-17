package com.app.ecommerce.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {

    ProductDto save(ProductDto productDto);

    ProductDto findById(UUID productId);

    Page<ProductDto> findAll(String name, Double minPrice, Double maxPrice, UUID categoryId, Pageable pageable);

    ProductDto updateById(UUID productId, ProductDto updatedProductDto);

    void deleteById(UUID productId);

}
