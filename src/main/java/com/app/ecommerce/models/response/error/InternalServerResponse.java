package com.app.ecommerce.models.response.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class InternalServerResponse {

    // as an example
    @Schema(type = "string" , example = "Id Not Found to Perform Function")
    private String message;
}
