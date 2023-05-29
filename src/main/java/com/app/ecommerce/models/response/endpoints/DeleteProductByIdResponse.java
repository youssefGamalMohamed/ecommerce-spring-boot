package com.app.ecommerce.models.response.endpoints;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DeleteProductByIdResponse {

    @Schema(type = "string" , example = "Product Deleted Successfully")
    private String message;
}
