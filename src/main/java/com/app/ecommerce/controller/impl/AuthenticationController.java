package com.app.ecommerce.controller.impl;

import com.app.ecommerce.controller.framework.IAuthenticationController;
import com.app.ecommerce.exception.type.JsonParsingException;
import com.app.ecommerce.models.request.LoginRequestBody;
import com.app.ecommerce.models.request.RegisterRequestBody;
import com.app.ecommerce.service.impl.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController implements IAuthenticationController {

    @Autowired
    private AuthenticationService service;


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestBody registerRequestBody) throws JsonParsingException {
        return ResponseEntity.ok(service.register(registerRequestBody));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestBody loginRequestBody) {
        return ResponseEntity.ok(service.authenticate(loginRequestBody));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return ResponseEntity.ok(service.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(service.logout());
    }


}
