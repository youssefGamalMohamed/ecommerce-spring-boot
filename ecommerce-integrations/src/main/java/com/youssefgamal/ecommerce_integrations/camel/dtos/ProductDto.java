package com.youssefgamal.ecommerce_integrations.camel.dtos;


import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ProductDto {
	
    private Long id;

    @NotBlank(message = "Name Must Not Be Empty Or NULL")
    private String name;

    @NotBlank(message = "Name Must Not Be Empty Or NULL")
    private String description;

    @Min(1)
    @Max(100000L)
    private double price;

    @Min(1)
    @Max(100000)
    private Integer quantity;
    
    private Set<CategoryDto> categories = new HashSet<>();
    
}
