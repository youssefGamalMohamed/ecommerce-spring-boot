package com.app.ecommerce.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Base response containing common audit fields")
public abstract class BaseResponse {

    @Schema(description = "Timestamp when the resource was created", accessMode = Schema.AccessMode.READ_ONLY, example = "2024-01-15T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the resource was last updated", accessMode = Schema.AccessMode.READ_ONLY, example = "2024-01-15T10:30:00Z")
    private Instant updatedAt;

    @Schema(description = "User who created the resource", accessMode = Schema.AccessMode.READ_ONLY, example = "admin")
    private String createdBy;

    @Schema(description = "User who last updated the resource", accessMode = Schema.AccessMode.READ_ONLY, example = "admin")
    private String updatedBy;
}
