package com.app.ecommerce.mappers;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.app.ecommerce.dtos.ProductDto;
import com.app.ecommerce.entity.Product;

@Mapper(uses = {CategoryMapper.class})
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    Product mapToEntity(ProductDto ProductDto);  
    ProductDto mapToDto(Product Product);
    List<ProductDto> mapToDtos(List<Product> categories);
    Set<ProductDto> mapToDtos(Set<Product> categories);
}
