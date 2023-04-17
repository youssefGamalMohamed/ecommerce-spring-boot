package com.app.ecommerce.models.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;


@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductDTO {

	private Long id;
	
    @NotBlank(message = "Name of Category Should Not Be Null or Empty")
    private String name;


    @NotBlank(message = "description of Category Should Not Be Null or Empty")
    private String description;

    @Min(5)
    private double price;

    @NotNull
    private Set<Long> categoriesId;

}
