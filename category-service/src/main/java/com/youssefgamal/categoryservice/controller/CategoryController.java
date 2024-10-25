package com.youssefgamal.categoryservice.controller;

import java.util.Collection;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.youssefgamal.categoryservice.entity.Category;
import com.youssefgamal.categoryservice.inputs.CategoryInput;
import com.youssefgamal.categoryservice.mappers.CategoryMapper;
import com.youssefgamal.categoryservice.service.CategoryServiceIfc;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Controller
@Slf4j
@RequiredArgsConstructor
public class CategoryController {


    private CategoryServiceIfc categoryService;
	private CategoryMapper categoryMapper;
	
    // Query: Find all categories
	@QueryMapping(name = "findAllCategories")
	public Collection<Category> findAllCategories() {
		log.info("findAllCategories()");
        Collection<Category> categories = categoryService.findAll();
        log.info("findAllCategories(): " + categories);
        return categories;
    }

    // Query: Find category by ID
	@QueryMapping(name = "findCategoryById")
    public Category findCategoryById(@Argument Long id) {
		log.info("findCategoryById({})" , id);
        Category category = categoryService.findById(id);
        log.info("findCategoryById({}): {}", id, category);
        return category;
    }

    // Mutation: Create category
	@MutationMapping(name = "createCategory")
    public Category createCategory(@Argument(name = "category")@Valid CategoryInput categoryInput) {
		log.info("createCategory(): " + categoryInput);
        Category newCreatedCategory = categoryService.save(categoryMapper.mapToEntity(categoryInput));
        log.info("createCategory(): " + newCreatedCategory);
        return newCreatedCategory;
    }

    // Mutation: Update category
	@MutationMapping(name = "updateCategory")
    public Category updateCategory(@Argument Long id, @Argument(name = "category") @Valid CategoryInput updatedBody) {
		log.info("updateCategory(id: {}, updatedBody: {})", id, updatedBody);
        Category updatedCategory = categoryService.updateById(id, categoryMapper.mapToEntity(updatedBody));
        log.info("updateCategory(id: {}, updatedBody: {}): {}", id, updatedBody, updatedCategory);
        return updatedCategory;
    }

    // Mutation: Delete category
	@MutationMapping(name = "deleteCategory")
    public boolean deleteCategory(@Argument Long id) {
		log.info("deleteCategory({})", id);
        categoryService.deleteById(id);
        log.info("deleteCategory({}): true", id);
        return true; // Return true if deletion is successful
    }


}
