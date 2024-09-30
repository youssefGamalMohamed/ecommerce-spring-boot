package com.app.ecommerce.dtos;



import java.util.HashSet;
import java.util.Set;

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
public class ProductDto {

    private Long id;
    private String name;
    private String description;
    private double price;
    private Integer quantity;
    private Set<CategoryDto> categories = new HashSet<>();    
}
