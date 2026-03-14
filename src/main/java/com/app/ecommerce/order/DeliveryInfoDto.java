package com.app.ecommerce.order;

import com.app.ecommerce.shared.dto.BaseDto;
import com.app.ecommerce.shared.enums.Status;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
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
@Schema(description = "Delivery information data transfer object")
public class DeliveryInfoDto extends BaseDto {

    @Schema(description = "Current delivery status", example = "PENDING")
    private Status status;

    @Schema(description = "Delivery address", example = "123 Main Street, City, Country")
    private String address;

    @Schema(description = "Expected delivery date", example = "2024-01-20")
    private String date;
}
