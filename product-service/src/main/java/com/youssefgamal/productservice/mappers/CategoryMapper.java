package com.youssefgamal.productservice.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.youssefgamal.productservice.dtos.CategoryInput;
import com.youssefgamal.productservice.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    
    @Mapping(source = "id", target = "id")
    Category mapToEntity(CategoryInput categoryInput);
    
    
    @Mapping(source = "id", target = "id")
    CategoryInput mapToInput(Category category);
    
    List<CategoryInput> mapToDtos(List<Category> categories);
}
