package com.app.ecommerce.category;

import com.app.ecommerce.shared.exception.DuplicatedUniqueColumnValueException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface CategoryService {

    CategoryDto save(CategoryDto categoryDto) throws DuplicatedUniqueColumnValueException;

    void deleteById(UUID categoryId);

    Page<CategoryDto> findAll(String name, Pageable pageable);

    CategoryDto findById(UUID categoryId);

    CategoryDto updateById(UUID categoryId, CategoryDto updatedCategoryDto);

    Set<Category> getCategories(Set<UUID> categories_ids);
}
