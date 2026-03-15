package com.app.ecommerce.category;

import com.app.ecommerce.shared.constants.CacheConstants;
import com.app.ecommerce.shared.exception.DuplicatedUniqueColumnValueException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @CacheEvict(value = CacheConstants.CATEGORIES, allEntries = true)
    public CategoryDto save(CategoryDto categoryDto) throws DuplicatedUniqueColumnValueException {
        log.info("Saving new category with name = {}", categoryDto.getName());

        if (categoryRepository.findByName(categoryDto.getName()).isPresent()) {
            throw new DuplicatedUniqueColumnValueException("Category Name Already Exist and Should Not Be Duplicated");
        }

        Category categoryToSave = categoryMapper.mapToEntity(categoryDto);
        Category newCreatedCategory = categoryRepository.save(categoryToSave);
        log.info("Category added/saved successfully with id = {}", newCreatedCategory.getId());

        return categoryMapper.mapToDto(newCreatedCategory);
    }

    @Override
    @CacheEvict(value = CacheConstants.CATEGORIES, allEntries = true)
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
    @Cacheable(value = CacheConstants.CATEGORIES, key = "'all'")
    public List<CategoryDto> findAll() {
        log.info("findAll() - Retrieving all categories");
        return categoryMapper.mapToDtos(categoryRepository.findAll());
    }

    @Override
    @Cacheable(value = CacheConstants.CATEGORIES, key = "#categoryId")
    public CategoryDto findById(UUID categoryId) {
        log.info("findById({})", categoryId);
        if (categoryId == null) {
            throw new IllegalArgumentException("Category Id Not Exist to Retrieve");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException(
                        "No Category To Retrieve, Id Not Found with value = " + categoryId));

        return categoryMapper.mapToDto(category);
    }

    @Override
    @CacheEvict(value = CacheConstants.CATEGORIES, allEntries = true)
    public CategoryDto updateById(UUID categoryId, CategoryDto updatedCategoryDto) {
        log.info("updateById({}, {})", categoryId, updatedCategoryDto);
        if (categoryId == null || updatedCategoryDto == null) {
            throw new IllegalArgumentException("Category Id Not Exist to Update");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("No Category Update, Id Not Found"));

        Category tempCategory = categoryMapper.mapToEntity(updatedCategoryDto);
        categoryMapper.updateFrom(tempCategory, category);

        Category updCategory = categoryRepository.save(category);

        log.info("updated category with id = {}", updCategory.getId());

        return categoryMapper.mapToDto(updCategory);
    }

    @Override
    public Set<Category> getCategories(Set<UUID> categories_ids) {
        log.info("get categories with ids = {}", categories_ids);
        return categoryRepository.findByIdIn(categories_ids);
    }

}
