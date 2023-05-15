package com.app.ecommerce.security.service;


import com.app.ecommerce.entity.User;
import com.app.ecommerce.enums.Role;
import com.app.ecommerce.models.request.LoginRequestBody;
import com.app.ecommerce.models.request.RegisterRequestBody;
import com.app.ecommerce.models.response.success.LoginResponseBody;
import com.app.ecommerce.models.response.success.RegisterResponseBody;
import com.app.ecommerce.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepo repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public RegisterResponseBody register(RegisterRequestBody request) {
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return RegisterResponseBody.builder()
                .token(jwtToken)
                .build();
    }

    public LoginResponseBody authenticate(LoginRequestBody request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return LoginResponseBody.builder()
                .token(jwtToken)
                .build();
    }
}
