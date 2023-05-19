package com.app.ecommerce.models.response.http;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class UnAuthorizedResponse {
    // as an example
    @Schema(type = "string" , example = "Failed to Login Invalid Username or Password")
    private String message;
}