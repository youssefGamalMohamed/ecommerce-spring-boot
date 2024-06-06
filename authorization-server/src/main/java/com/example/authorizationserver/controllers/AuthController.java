package com.example.authorizationserver.controllers;


import com.example.authorizationserver.models.RegisterNewUserRequestBody;
import com.example.authorizationserver.services.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/register")
    public ResponseEntity<HttpStatus> registerNewUser(@Valid @RequestBody RegisterNewUserRequestBody registerNewUserRequestBody) {
        boolean isRegistrationDone = authService.registerNewUser(registerNewUserRequestBody);
        return (isRegistrationDone) ? new ResponseEntity<>(HttpStatus.CREATED) : new ResponseEntity<>(HttpStatus.CONFLICT);
    }

}
