package com.app.ecommerce.category;

import com.app.ecommerce.shared.constants.CacheConstants;
import com.app.ecommerce.shared.exception.DuplicatedUniqueColumnValueException;
import com.app.ecommerce.shared.util.SortUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @CachePut(value = CacheConstants.CATEGORIES, key = "#result.id")
    @Transactional
    public CategoryResponse save(CreateCategoryRequest request) throws DuplicatedUniqueColumnValueException {
        log.info("Saving new category with name = {}", request.getName());

        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicatedUniqueColumnValueException("Category Name Already Exist and Should Not Be Duplicated");
        }

        Category categoryToSave = categoryMapper.mapToEntity(request);
        Category newCreatedCategory = categoryRepository.saveAndFlush(categoryToSave);
        log.info("Category added/saved successfully with id = {}", newCreatedCategory.getId());

        return categoryMapper.mapToResponse(newCreatedCategory);
    }

    @Override
    @CacheEvict(value = CacheConstants.CATEGORIES, key = "#categoryId")
    @Transactional
    public void deleteById(UUID categoryId) {
        log.info("deleteById({})", categoryId);
        
        int deleted = categoryRepository.deleteCategoryById(categoryId);
        if (deleted == 0) {
            throw new NoSuchElementException("Category with id " + categoryId + " not found");
        }

        log.info("Category deleted successfully with id = {}", categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> findAll(String name, Pageable pageable) {
        log.info("findAll(name={}, pageable={})", name, pageable);

        Sort safeSort = SortUtils.sanitize(pageable.getSort(), Set.of("name", "createdAt"), Sort.Order.asc("name"));
        PageRequest safePage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);

        Specification<Category> spec = CategorySpecifications.nameLike(name);

        Page<CategoryResponse> result = categoryRepository.findAll(spec, safePage).map(categoryMapper::mapToResponse);
        log.info("findAll(): Found {} categories", result.getTotalElements());
        return result;
    }

    @Override
    @Cacheable(value = CacheConstants.CATEGORIES, key = "#categoryId")
    @Transactional(readOnly = true)
    public CategoryResponse findById(UUID categoryId) {
        log.info("findById({})", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Category with id " + categoryId + " not found"));

        return categoryMapper.mapToResponse(category);
    }

    @Override
    @CachePut(value = CacheConstants.CATEGORIES, key = "#result.id")
    @Transactional
    public CategoryResponse updateById(UUID categoryId, UpdateCategoryRequest request) {
        log.info("updateById({}, {})", categoryId, request);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Category with id " + categoryId + " not found"));


        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.findByName(request.getName()).isPresent()) {
                throw new DuplicatedUniqueColumnValueException("Category Name Already Exist and Should Not Be Duplicated");
            }
        }

        categoryMapper.updateEntityFromRequest(request, category);

        Category updCategory = categoryRepository.saveAndFlush(category);

        log.info("updated category with id = {}", updCategory.getId());

        return categoryMapper.mapToResponse(updCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Category> getCategories(Set<UUID> categories_ids) {
        log.info("get categories with ids = {}", categories_ids);
        Set<Category> found = categoryRepository.findByIdIn(categories_ids);
        if (found.size() != categories_ids.size()) {
            throw new NoSuchElementException("One or more category IDs do not exist");
        }
        return found;
    }

}
