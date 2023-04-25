package com.app.ecommerce.models.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

import com.app.ecommerce.entity.Category;
import com.app.ecommerce.entity.Product;



@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class PostProductRequestBody {

	private Long id;
	
    @NotBlank(message = "Name of Category Should Not Be Null or Empty")
    private String name;


    @NotBlank(message = "description of Category Should Not Be Null or Empty")
    private String description;

    @Min(5)
    private double price;

    @Min(1)
    private Integer quantity;
    
    @NotNull
    private Set<Long> categoriesId;

   
}
