package com.app.ecommerce.mappers;

import com.app.ecommerce.dtos.CategoryDto;
import com.app.ecommerce.entity.Category;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    @Mapping(target = "products", ignore = true)
    Category mapToEntity(CategoryDto categoryDto);  
    CategoryDto mapToDto(Category category);
    List<CategoryDto> mapToDtos(List<Category> categories);
}
