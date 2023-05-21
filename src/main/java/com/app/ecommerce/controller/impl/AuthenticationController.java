package com.app.ecommerce.controller.impl;

import com.app.ecommerce.controller.framework.IAuthenticationController;
import com.app.ecommerce.models.request.LoginRequestBody;
import com.app.ecommerce.models.request.RegisterRequestBody;
import com.app.ecommerce.security.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController implements IAuthenticationController {

    @Autowired
    private AuthenticationService service;


    @PostMapping("/sign-up")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestBody registerRequestBody) {
        return ResponseEntity.ok(service.register(registerRequestBody));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@Valid @RequestBody LoginRequestBody loginRequestBody) {
        return ResponseEntity.ok(service.authenticate(loginRequestBody));
    }


}
