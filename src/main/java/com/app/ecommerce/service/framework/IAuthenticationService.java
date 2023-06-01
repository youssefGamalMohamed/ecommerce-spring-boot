package com.app.ecommerce.service.framework;

import com.app.ecommerce.entity.User;
import com.app.ecommerce.models.request.LoginRequestBody;
import com.app.ecommerce.models.request.RegisterRequestBody;
import com.app.ecommerce.models.response.endpoints.LoginResponseBody;
import com.app.ecommerce.models.response.endpoints.LogoutResponseBody;
import com.app.ecommerce.models.response.endpoints.RefreshTokenResponseBody;
import com.app.ecommerce.models.response.endpoints.RegisterResponseBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface IAuthenticationService {

    RegisterResponseBody register(RegisterRequestBody request);

    LoginResponseBody authenticate(LoginRequestBody request);


    void saveUserToken(User user, String jwtToken);

    void revokeAllUserTokens(User user);


    RefreshTokenResponseBody refreshToken() throws IOException;

    LogoutResponseBody logout();



}
