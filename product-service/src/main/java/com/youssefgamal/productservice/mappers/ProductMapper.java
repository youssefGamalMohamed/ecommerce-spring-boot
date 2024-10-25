package com.youssefgamal.productservice.mappers;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;

import com.youssefgamal.productservice.dtos.ProductInput;
import com.youssefgamal.productservice.entity.Product;


@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ProductMapper {
	
    Product mapToEntity(ProductInput productInput);  
    ProductInput mapToInput(Product product);
    List<ProductInput> mapToInputs(List<Product> categories);
    Set<ProductInput> mapToInputs(Set<Product> categories);
}
