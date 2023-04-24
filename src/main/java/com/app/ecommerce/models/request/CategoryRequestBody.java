package com.app.ecommerce.models.request;

import com.app.ecommerce.entity.Category;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;



@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class CategoryRequestBody {


    private Long id;

    @NotBlank(message = "Name of Category Should Not Be Null or Empty")
    private String name;
    
}
