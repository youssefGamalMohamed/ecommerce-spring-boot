package com.app.ecommerce.models.request;

import com.app.ecommerce.enums.Role;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestBody {

    @NotBlank(message = "First Name Should Not Be Null or Empty")
    private String firstname;

    @NotBlank(message = "Last Name Should Not Be Null or Empty")
    private String lastname;

    @NotBlank(message = "Email Should Not Be Null or Empty")
    private String email;

    @NotBlank(message = "Password of Category Should Not Be Null or Empty")
    private String password;

    private Role role;
}
