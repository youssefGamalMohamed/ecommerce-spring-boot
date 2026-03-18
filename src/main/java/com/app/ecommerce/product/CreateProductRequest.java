package com.app.ecommerce.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request to create a new product")
public class CreateProductRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "Name of the product", example = "iPhone 15 Pro", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Detailed description of the product", example = "Latest Apple smartphone with advanced features")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be zero or positive")
    @Schema(description = "Price of the product", example = "999.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be zero or greater")
    @Schema(description = "Available quantity in stock", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @NotEmpty(message = "At least one category is required")
    @Schema(description = "Set of category IDs this product belongs to", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<UUID> categoryIds;
}
