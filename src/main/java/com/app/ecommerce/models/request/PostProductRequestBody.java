package com.app.ecommerce.models.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.Set;



@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class PostProductRequestBody {

    @NotBlank(message = "Name Should Not Be Null or Empty")
    private String name;


    @NotBlank(message = "Description of Category Should Not Be Null or Empty")
    private String description;

    @Min(5)
    @NotNull(message = "Price should not be empty or null")
    private double price;

    @Min(1)
    @NotNull(message = "Quantity should not be empty or null")
    private Integer quantity;
    
    @NotNull(message = "Categories Id's should Not be Null or Empty")
    private Set<Long> categoriesId;

   
}
