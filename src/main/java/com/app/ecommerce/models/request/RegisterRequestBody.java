package com.app.ecommerce.models.request;

import com.app.ecommerce.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestBody {

    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Role role;
}
