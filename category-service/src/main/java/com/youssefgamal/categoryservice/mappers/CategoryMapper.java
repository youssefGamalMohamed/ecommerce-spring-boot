package com.youssefgamal.categoryservice.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.youssefgamal.categoryservice.entity.Category;
import com.youssefgamal.categoryservice.inputs.CategoryInput;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
	
    Category mapToEntity(CategoryInput categoryDto);
    CategoryInput mapToInput(Category category);
    List<CategoryInput> mapToDtos(List<Category> categories);
    
}
