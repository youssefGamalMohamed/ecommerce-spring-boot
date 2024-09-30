package com.youssefgamal.categoryservice.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.youssefgamal.categoryservice.dtos.CategoryDto;
import com.youssefgamal.categoryservice.entity.Category;

@Mapper
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    Category mapToEntity(CategoryDto categoryDto);
    CategoryDto mapToDto(Category category);
    List<CategoryDto> mapToDtos(List<Category> categories);
}
