package com.app.ecommerce.product;

import com.app.ecommerce.category.CategoryDto;
import com.app.ecommerce.shared.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
@Schema(description = "Product data transfer object")
public class ProductDto extends BaseDto {

    @Schema(description = "Unique identifier of the product", accessMode = Schema.AccessMode.READ_ONLY, example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Name of the product", example = "iPhone 15 Pro", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Detailed description of the product", example = "Latest Apple smartphone with advanced features")
    private String description;

    @Schema(description = "Price of the product", example = "999.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private double price;

    @Schema(description = "Available quantity in stock", example = "50")
    private Integer quantity;

    @Schema(description = "Set of categories this product belongs to")
    @Builder.Default
    private Set<CategoryDto> categories = new HashSet<>();
}
