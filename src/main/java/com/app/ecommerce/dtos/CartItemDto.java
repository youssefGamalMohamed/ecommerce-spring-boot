package com.app.ecommerce.dtos;



import com.app.ecommerce.entity.Product;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemDto {
	
    private Long id;
	private int productQuantity;
	private ProductDto product;
}
