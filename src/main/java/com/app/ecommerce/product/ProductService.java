package com.app.ecommerce.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductService {

    ProductResponse save(CreateProductRequest request);

    ProductResponse findById(UUID productId);

    Page<ProductResponse> findAll(String name, BigDecimal minPrice, BigDecimal maxPrice, UUID categoryId, Pageable pageable);

    ProductResponse updateById(UUID productId, UpdateProductRequest request);

    void deleteById(UUID productId);

}
