package com.app.ecommerce.models.request;

import com.app.ecommerce.enums.Status;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class CategoryDTO {


    private Long id;

    @NotBlank(message = "Name of Category Should Not Be Null or Empty")
    private String name;
}
