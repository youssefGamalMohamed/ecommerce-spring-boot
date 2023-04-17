package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.Category;
import com.app.ecommerce.exception.models.IdNotFoundException;
import com.app.ecommerce.models.request.CategoryDTO;

import java.util.Set;

public interface ICategoryService {
    boolean addCategory(CategoryDTO category);

    boolean deleteCategoryById(Long categoryId) throws IdNotFoundException;

    Set<CategoryDTO> getAllCategories();
}
