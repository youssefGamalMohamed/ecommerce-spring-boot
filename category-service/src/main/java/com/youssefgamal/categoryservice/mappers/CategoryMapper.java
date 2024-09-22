package com.youssefgamal.categoryservice.mappers;

import com.youssefgamal.categoryservice.dtos.CategoryDto;
import com.youssefgamal.categoryservice.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    Category mapToEntity(CategoryDto categoryDto);
    CategoryDto mapToDto(Category category);
    List<CategoryDto> mapToDtos(List<Category> categories);
}
