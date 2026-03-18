package com.app.ecommerce.auth;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
}
