package com.app.ecommerce.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Request to update an existing product")
public class UpdateProductRequest {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "Name of the product", example = "iPhone 15 Pro")
    private String name;

    @Schema(description = "Detailed description of the product", example = "Latest Apple smartphone with advanced features")
    private String description;

    @DecimalMin(value = "0.00", message = "Price must be zero or positive")
    @Schema(description = "Price of the product", example = "999.99")
    private BigDecimal price;

    @Min(value = 0, message = "Quantity must be zero or greater")
    @Schema(description = "Available quantity in stock", example = "50")
    private Integer quantity;

    @Schema(description = "Set of category IDs this product belongs to")
    private Set<UUID> categoryIds;

    @NotNull(message = "Version is required for optimistic locking")
    @Schema(description = "Version for optimistic locking", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long version;
}
