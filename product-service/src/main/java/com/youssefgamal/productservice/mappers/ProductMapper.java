package com.youssefgamal.productservice.mappers;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.youssefgamal.productservice.dtos.ProductDto;
import com.youssefgamal.productservice.entity.Product;


@Mapper(uses = {CategoryMapper.class})
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    Product mapToEntity(ProductDto ProductDto);  
    ProductDto mapToDto(Product Product);
    List<ProductDto> mapToDtos(List<Product> categories);
    Set<ProductDto> mapToDtos(Set<Product> categories);
}
