package com.app.ecommerce.models.response.endpoints;

import lombok.*;

import java.util.List;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GetAllCustomersResponse {
    List<GetCustomerResponse> customers;
}
