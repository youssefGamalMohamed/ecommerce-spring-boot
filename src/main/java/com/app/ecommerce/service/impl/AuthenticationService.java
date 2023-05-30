package com.app.ecommerce.service.impl;




import com.app.ecommerce.entity.Token;
import com.app.ecommerce.entity.User;
import com.app.ecommerce.enums.TokenType;
import com.app.ecommerce.exception.type.DuplicatedUniqueColumnValueException;
import com.app.ecommerce.exception.type.MissingRefreshTokenException;
import com.app.ecommerce.factory.UserFactory;
import com.app.ecommerce.models.request.LoginRequestBody;
import com.app.ecommerce.models.request.RegisterRequestBody;
import com.app.ecommerce.models.response.endpoints.LoginResponseBody;
import com.app.ecommerce.models.response.endpoints.LogoutResponseBody;
import com.app.ecommerce.models.response.endpoints.RefreshTokenResponseBody;
import com.app.ecommerce.models.response.endpoints.RegisterResponseBody;
import com.app.ecommerce.repository.TokenRepo;
import com.app.ecommerce.repository.UserRepo;
import com.app.ecommerce.service.framework.IAuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {
    private final UserRepo userRepo;
    private final TokenRepo tokenRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final UserFactory userFactory;

    private final LogoutHandler logoutHandler;


    public RegisterResponseBody register(RegisterRequestBody request) {

        if(userRepo.existsByEmail(request.getEmail()))
            throw new DuplicatedUniqueColumnValueException("Email Already Exist Before , Try another one");

        var user = userFactory.getUser(request);

        var savedUser = userRepo.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return RegisterResponseBody.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public LoginResponseBody authenticate(LoginRequestBody request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepo.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return LoginResponseBody.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepo.save(token);
    }

    public void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepo.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepo.saveAll(validUserTokens);
    }

    public RefreshTokenResponseBody refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            throw new MissingRefreshTokenException("missing Refresh-Token in the Headers");
        }


        refreshToken = authHeader.substring(7);
        try {
            userEmail = jwtService.extractUsername(refreshToken);
        }
        catch (Exception exception) {
            throw new BadCredentialsException("corrupted or invalid Refresh-Token in the Headers");
        }



        if (userEmail != null) {
            var user = this.userRepo.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                return RefreshTokenResponseBody.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();

            } else {
                throw new CredentialsExpiredException("expired refresh token");
            }
        }
        else {
            throw new BadCredentialsException("corrupted or invalid Refresh-Token in the Headers");
        }
    }


    public LogoutResponseBody logout(HttpServletRequest request, HttpServletResponse response) {

        logoutHandler.logout(request , response , SecurityContextHolder.getContext().getAuthentication());

        return LogoutResponseBody.builder()
                .message("Logout Done Successfully")
                .build();
    }
}