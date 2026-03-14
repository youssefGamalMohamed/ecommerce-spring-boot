package com.app.ecommerce.category;

import com.app.ecommerce.shared.exception.type.DuplicatedUniqueColumnValueException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public Category save(Category category) throws DuplicatedUniqueColumnValueException {
        log.info("Saving new category with name = {}", category.getName());

        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new DuplicatedUniqueColumnValueException("Category Name Already Exist and Should Not Be Duplicated");
        }

        Category newCreatedCategory = categoryRepository.save(category);
        log.info("Category added/saved successfully with id = {}", newCreatedCategory.getId());

        return newCreatedCategory;
    }

    @Override
    public void deleteById(UUID categoryId) {
        log.info("deleteById({})", categoryId);
        if (categoryId == null) {
            throw new IllegalArgumentException("Category Id Not Exist to Delete");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Category Id Not Exist to Delete"));

        log.info("Category exists with id = {}", category.getId());
        categoryRepository.deleteById(categoryId);

        log.info("Category deleted successfully with id = {}", categoryId);
    }

    @Override
    public List<Category> findAll() {
        log.info("findAll() - Retrieving all categories");
        return categoryRepository.findAll();
    }

    @Override
    public Category findById(UUID categoryId) {
        log.info("findById({})", categoryId);
        if (categoryId == null) {
            throw new IllegalArgumentException("Category Id Not Exist to Retrieve");
        }

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException(
                        "No Category To Retrieve, Id Not Found with value = " + categoryId));
    }

    @Override
    public Category updateById(UUID categoryId, Category updatedCategory) {
        log.info("updateById({}, {})", categoryId, updatedCategory);
        if (categoryId == null || updatedCategory == null) {
            throw new IllegalArgumentException("Category Id Not Exist to Update");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("No Category Update, Id Not Found"));

        categoryMapper.updateFrom(updatedCategory, category);

        Category updCategory = categoryRepository.save(category);

        log.info("updated category with id = {}", updCategory.getId());

        return updCategory;
    }

    @Override
    public Set<Category> getCategories(Set<UUID> categories_ids) {
        log.info("get categories with ids = {}", categories_ids);
        return categoryRepository.findByIdIn(categories_ids);
    }

}
