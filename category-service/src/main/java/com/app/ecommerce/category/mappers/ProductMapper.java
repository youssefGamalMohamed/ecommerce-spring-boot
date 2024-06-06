package com.app.ecommerce.category.mappers;


import com.app.ecommerce.category.dtos.ProductDto;
import com.app.ecommerce.category.entities.Product;

import java.util.Set;
import java.util.stream.Collectors;

public class ProductMapper {

    public static Product toEntity(ProductDto productDto) {
       return Product.builder()
               .id(productDto.getId())
               .name(productDto.getName())
               .price(productDto.getPrice())
               .description(productDto.getDescription())
               .quantity(productDto.getQuantity())
               .build();
    }

    public static ProductDto toDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .quantity(product.getQuantity())
                .build();
    }

    public static Set<ProductDto> toDto(Set<Product> products) {
        return products.stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toSet());
    }

    public static Set<Product> toEntity(Set<ProductDto> products) {
        return products.stream()
                .map(ProductMapper::toEntity)
                .collect(Collectors.toSet());
    }
}
