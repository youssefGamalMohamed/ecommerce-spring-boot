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
@Schema(description = "Delivery information response")
public class DeliveryInfoResponse {

    @Schema(description = "Current delivery status", example = "NOT_MOVED_OUT_FROM_WAREHOUSE")
    private Status status;

    @Schema(description = "Delivery address", example = "123 Main Street, City, Country")
    private String address;

    @Schema(description = "Expected delivery date", example = "2024-01-20")
    private LocalDate date;
}
