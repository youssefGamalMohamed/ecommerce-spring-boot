package com.app.ecommerce.models.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class UserRequestBody {

    @NotBlank(message = "First Name Should Not Be Null or Empty")
    private String firstName;

    @NotBlank(message = "Second Name Should Not Be Null or Empty")
    private String lastName;
}
