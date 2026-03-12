package com.app.ecommerce.service.impl;

import com.app.ecommerce.entity.Category;
import com.app.ecommerce.exception.type.DuplicatedUniqueColumnValueException;
import com.app.ecommerce.exception.type.IdNotFoundException;
import com.app.ecommerce.mappers.CategoryMapper;
import com.app.ecommerce.repository.CategoryRepo;
import com.app.ecommerce.service.framework.ICategoryService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CategoryService implements ICategoryService {

	private final CategoryRepo categoryRepo;
	private final CategoryMapper categoryMapper;

	@Override
	public Category save(Category category) throws DuplicatedUniqueColumnValueException {
		log.info("Saving new category with name = {}", category.getName());

		if (categoryRepo.findByName(category.getName()).isPresent())
			throw new DuplicatedUniqueColumnValueException("Category Name Already Exist and Should Not Be Duplicated");

		Category newCreatedCategory = categoryRepo.save(category);
		log.info("Category added/saved succesffully with id = {}", newCreatedCategory.getId());

		return newCreatedCategory;
	}

	@Override
	public void deleteById(Long categoryId) throws IdNotFoundException {
		log.info("deleteById({})", categoryId);
		if (categoryId == null)
			throw new IllegalArgumentException("Category Id Not Exist to Delete");

		Category category = categoryRepo.findById(categoryId)
				.orElseThrow(() -> new IdNotFoundException("Category Id Not Exist to Delete"));

		log.info("Category exists with id = {}", category.getId());
		categoryRepo.deleteById(categoryId);

		log.info("Category deleted successfully with id = {}", categoryId);
	}

	@Override
	public List<Category> findAll() {
		log.info("findAll() - Retrieving all categories");
		return categoryRepo.findAll();
	}

	@Override
	public Category findById(Long categoryId) {
		log.info("findById({})", categoryId);
		if (categoryId == null)
			throw new IllegalArgumentException("Category Id Not Exist to Retrieve");

		return categoryRepo.findById(categoryId)
				.orElseThrow(() -> new IdNotFoundException(
						"No Category To Retrieve, Id Not Found with value = " + categoryId));
	}

	@Override
	public Category updateById(Long categoryId, Category updatedCategory) {
		log.info("updateById({}, {})", categoryId, updatedCategory);
		if (categoryId == null || updatedCategory == null)
			throw new IllegalArgumentException("Category Id Not Exist to Update");

		Category category = categoryRepo.findById(categoryId)
				.orElseThrow(() -> new IdNotFoundException("No Category Update, Id Not Found"));

		categoryMapper.updateFrom(updatedCategory, category);

		Category updCategory = categoryRepo.save(category);

		log.info("updated category with id = {}", updCategory.getId());

		return updCategory;
	}

	@Override
	public Set<Category> getCategories(Set<Long> categories_ids) {
		log.info("get cateogries with ids = {}", categories_ids);
		return categoryRepo.findByIdIn(categories_ids);
	}

}
