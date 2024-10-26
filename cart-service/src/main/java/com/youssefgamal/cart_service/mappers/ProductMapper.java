package com.youssefgamal.cart_service.mappers;

import java.util.Collection;

import org.mapstruct.Mapper;

import com.youssefgamal.cart_service.dtos.ProductInput;
import com.youssefgamal.cart_service.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

	ProductInput mapToInput(Product product);
	Product mapToEntity(ProductInput productInput);
	Collection<Product> mapToEntities(Collection<Product> products);
	Collection<ProductInput> mapToInputs(Collection<ProductInput> productInputs);
	
}
