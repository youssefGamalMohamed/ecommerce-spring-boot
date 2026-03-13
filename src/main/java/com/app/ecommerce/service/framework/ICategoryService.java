package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.Category;
import com.app.ecommerce.exception.type.DuplicatedUniqueColumnValueException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ICategoryService {

    Category save(Category category) throws DuplicatedUniqueColumnValueException;

    void deleteById(UUID categoryId);

    List<Category> findAll();

    Category findById(UUID categoryId);

    Category updateById(UUID categoryId, Category updatedCategory);

    Set<Category> getCategories(Set<UUID> categories_ids);
}
