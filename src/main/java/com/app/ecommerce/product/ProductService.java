package com.app.ecommerce.product;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProductService {

    ProductDto save(ProductDto productDto);

    Set<ProductDto> findAllByCategoryName(String categoryName);

    ProductDto findById(UUID productId);

    List<ProductDto> findAll();

    ProductDto updateById(UUID productId, ProductDto updatedProductDto);

    void deleteById(UUID productId);

}
