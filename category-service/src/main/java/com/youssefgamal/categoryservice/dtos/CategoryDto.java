package com.youssefgamal.categoryservice.dtos;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;


@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDto {
	
    private Long id;
    private String name;
    
}
