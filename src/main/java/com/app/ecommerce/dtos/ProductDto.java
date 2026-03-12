package com.app.ecommerce.dtos;



import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto extends BaseDto {

    private Long id;
    private String name;
    private String description;
    private double price;
    private Integer quantity;
    @Builder.Default
    private Set<CategoryDto> categories = new HashSet<>();    
}
