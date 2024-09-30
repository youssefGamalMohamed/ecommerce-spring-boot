package com.app.ecommerce.email.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VerificationRegistrationMessageDetails {
    private String email;
    private String firstName;
    private String lastName;
    private String verificationToken;
}
