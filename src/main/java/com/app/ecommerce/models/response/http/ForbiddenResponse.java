package com.app.ecommerce.models.response.http;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ForbiddenResponse {
    @Schema(type = "string" , example = "Id Not Found to Perform Function")
    private String message;
}
