package com.app.ecommerce.category;

import com.app.ecommerce.shared.exception.DuplicatedUniqueColumnValueException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse save(CreateCategoryRequest request) throws DuplicatedUniqueColumnValueException;

    void deleteById(UUID categoryId);

    Page<CategoryResponse> findAll(String name, Pageable pageable);

    CategoryResponse findById(UUID categoryId);

    CategoryResponse updateById(UUID categoryId, UpdateCategoryRequest request);

    Set<Category> getCategories(Set<UUID> categories_ids);
}
