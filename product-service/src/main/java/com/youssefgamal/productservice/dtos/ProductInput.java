package com.youssefgamal.productservice.dtos;



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
public class ProductInput {

    private Long id;
    
    @NotBlank(message = "Categroy Name Must Not Be Null or Empty")
    private String name;
    
    @NotBlank(message = "Categroy Description Must Not Be Null or Empty")
    private String description;
    
    @Min(1) @Max(Integer.MAX_VALUE)
    private double price;
    
    @Min(1) @Max(10000)
    private Integer quantity;
    
    private Set<CategoryInput> categories = new HashSet<>();    
}
