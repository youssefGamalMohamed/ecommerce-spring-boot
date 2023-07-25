package com.app.ecommerce.models.response.endpoints;

import com.app.ecommerce.entity.Order;
import lombok.*;

import java.util.List;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GetCustomerOrdersResponseBody {
    private List<Order> orders;
}
