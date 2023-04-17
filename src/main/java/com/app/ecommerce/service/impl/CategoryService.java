package com.app.ecommerce.service.impl;


import com.app.ecommerce.entity.Category;
import com.app.ecommerce.entity.Product;
import com.app.ecommerce.exception.models.IdNotFoundException;
import com.app.ecommerce.models.request.CategoryDTO;
import com.app.ecommerce.repository.CategoryRepo;
import com.app.ecommerce.repository.ProductRepo;
import com.app.ecommerce.service.framework.ICategoryService;
import com.app.ecommerce.service.framework.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    @Override
    public boolean addCategory(CategoryDTO categoryDTO) {
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .build();

        categoryRepo.save(category);
        return false;
    }

    @Override
    public boolean deleteCategoryById(Long categoryId) throws IdNotFoundException {
        if(!categoryRepo.existsById(categoryId))
            throw new IdNotFoundException("category not found to delete");

        categoryRepo.deleteById(categoryId);
        return true;
    }

    @Override
    public Set<CategoryDTO> getAllCategories() {
        return categoryRepo.findAll()
                .stream()
                .map(
                        category -> CategoryDTO.builder()
                                .name(category.getName())
                                .id(category.getId())
                                .build()
                )
                .collect(Collectors.toSet());
    }
}
