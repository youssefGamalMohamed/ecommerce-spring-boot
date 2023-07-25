package com.app.ecommerce.models.response.endpoints;

import lombok.*;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GetCustomerResponse {
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
}
