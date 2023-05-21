package com.app.ecommerce.models.request;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestBody {

    @NotBlank(message = "Email Should Not Be Null or Empty")
    private String email;

    @NotBlank(message = "Password Should Not Be Null or Empty")
    private String password;
}
