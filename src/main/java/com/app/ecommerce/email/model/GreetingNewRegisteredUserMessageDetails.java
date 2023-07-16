package com.app.ecommerce.email.model;

import lombok.*;

@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GreetingNewRegisteredUserMessageDetails {
    private String email;
    private String firstName;
    private String lastName;
}
