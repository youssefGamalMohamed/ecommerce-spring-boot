package com.app.ecommerce.order;

import com.app.ecommerce.shared.enums.Status;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request to update an existing order")
public class UpdateOrderRequest {

    @Schema(description = "Delivery status", example = "ON_THE_WAY_TO_CUSTOMER")
    private Status deliveryStatus;

    @Schema(description = "Delivery address", example = "123 Main Street, City, Country")
    private String deliveryAddress;

    @Schema(description = "Expected delivery date", example = "2024-01-20")
    private LocalDate deliveryDate;
}
