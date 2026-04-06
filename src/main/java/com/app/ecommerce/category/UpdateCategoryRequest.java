package com.app.ecommerce.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request to update an existing category")
public class UpdateCategoryRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "Name of the category", example = "Electronics")
    private String name;

    @Schema(description = "Version for optimistic locking", example = "1")
    private Long version;
}
