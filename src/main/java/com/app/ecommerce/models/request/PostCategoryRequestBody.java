package com.app.ecommerce.models.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;



@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class PostCategoryRequestBody {


    private Long id;

    @NotBlank(message = "Name of Category Should Not Be Null or Empty")
    private String name;
    
}
