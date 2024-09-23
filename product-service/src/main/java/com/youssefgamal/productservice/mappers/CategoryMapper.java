package com.youssefgamal.productservice.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.youssefgamal.productservice.dtos.CategoryDto;
import com.youssefgamal.productservice.entity.Category;

@Mapper
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    Category mapToEntity(CategoryDto categoryDto);  
    CategoryDto mapToDto(Category category);
    List<CategoryDto> mapToDtos(List<Category> categories);
}
