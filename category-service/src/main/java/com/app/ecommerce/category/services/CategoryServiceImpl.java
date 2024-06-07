package com.app.ecommerce.category.services;

import com.app.ecommerce.category.entities.Category;
import com.app.ecommerce.category.entities.Product;
import com.app.ecommerce.category.repos.CategoryRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;


@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryServiceInterface {

    private final CategoryRepo categoryRepo;

    @Override
    public Category save(Category category) throws NoSuchElementException {
        return categoryRepo.save(category);
    }

    @Override
    public void deleteById(Long categoryId) throws NoSuchElementException {
        categoryRepo.deleteById(categoryId);
    }

    @Override
    public List<Category> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return categoryRepo.findAll(pageable).getContent();
    }

    @Override
    public Category findById(Long categoryId) {
        return categoryRepo.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Category not found with id = " + categoryId));
    }

    @Override
    public Category updateById(Long categoryId, Category updatedCategory) {
        Category currentCategory = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Category not found with id = " + categoryId));
        currentCategory.setName(updatedCategory.getName());
        return categoryRepo.save(currentCategory);
    }
}
