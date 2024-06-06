package com.app.ecommerce.category.mappers;

import com.app.ecommerce.category.dtos.CategoryDto;
import com.app.ecommerce.category.entities.Category;

import java.util.Collection;
import java.util.stream.Collectors;

public class CategoryMapper {

    public static Category toEntity(CategoryDto categoryDto) {
        return Category.builder()
                .id(categoryDto.getId())
                .name(categoryDto.getName())
                .products(ProductMapper.toEntity(categoryDto.getProducts()))
                .build();
    }

    public static CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .products(ProductMapper.toDto(category.getProducts()))
                .build();
    }

    public static Collection<CategoryDto> toDto(Collection<Category> categories) {
        return categories.stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toSet());
    }
}
