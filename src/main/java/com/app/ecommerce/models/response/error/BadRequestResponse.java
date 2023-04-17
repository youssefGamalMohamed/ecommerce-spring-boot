package com.app.ecommerce.models.response.error;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;


@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class BadRequestResponse {

    @Schema(type = "string" , example = "Validation Failed")
    private String message;


    @ArraySchema(
            arraySchema = @Schema(
                example = "{\"attribute1\": [ \"attribute1 Should Not Be Null or Empty\" ], \"attribute2\": [ \"attribute2  Should Not Be Null or Empty\"]}"
            )
    )
    private Map<String,List<String>> failed_validation_attributes;
}
