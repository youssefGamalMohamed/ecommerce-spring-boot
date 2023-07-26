package com.app.ecommerce.models.response.http;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ServiceUnavailableResponse {

    // as an example
    @Schema(type = "string" , example = "Service is Currently Unavailable Try our System sooner")
    private String message;
}
