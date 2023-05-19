package com.app.ecommerce.models.response.other;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ConflictResponse {

    @Schema(type = "string" , example = "Element Already Exist and should be unique")
    private String message;
}
