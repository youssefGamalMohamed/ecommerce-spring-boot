package com.app.ecommerce.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.app.ecommerce.dtos.CategoryDto;
import com.app.ecommerce.entity.Category;

@Mapper
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    Category mapToEntity(CategoryDto categoryDto);  
    CategoryDto mapToDto(Category category);
    List<CategoryDto> mapToDtos(List<Category> categories);
}
