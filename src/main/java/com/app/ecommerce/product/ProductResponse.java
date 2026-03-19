package com.app.ecommerce.product;

import com.app.ecommerce.category.CategoryResponse;
import com.app.ecommerce.shared.models.BaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Product response")
public class ProductResponse extends BaseResponse {

    @Schema(description = "Unique identifier of the product", accessMode = Schema.AccessMode.READ_ONLY, example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Name of the product", example = "iPhone 15 Pro")
    private String name;

    @Schema(description = "Detailed description of the product", example = "Latest Apple smartphone with advanced features")
    private String description;

    @Schema(description = "Price of the product", example = "999.99")
    private BigDecimal price;

    @Schema(description = "Available quantity in stock", example = "50")
    private Integer quantity;

    @Schema(description = "Version for optimistic locking", example = "1")
    private Long version;

    @Schema(description = "Set of categories this product belongs to")
    @Builder.Default
    private Set<CategoryResponse> categories = new HashSet<>();
}
