package com.app.ecommerce.category;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.app.ecommerce.shared.exception.DuplicatedUniqueColumnValueException;

public interface CategoryService {

    CategoryDto save(CategoryDto categoryDto) throws DuplicatedUniqueColumnValueException;

    void deleteById(UUID categoryId);

    List<CategoryDto> findAll();

    CategoryDto findById(UUID categoryId);

    CategoryDto updateById(UUID categoryId, CategoryDto updatedCategoryDto);

    Set<Category> getCategories(Set<UUID> categories_ids);
}
