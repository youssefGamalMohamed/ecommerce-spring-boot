package com.youssefgamal.ecommerce_integrations.camel.dtos;


import com.fasterxml.jackson.annotation.JsonInclude;

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
public class CategoryDto {
	
    private Long id;
    
    @NotBlank(message = "Name Must Not Be Null or Empty")
    private String name;
    
}
