package com.app.ecommerce.models.response.success;

import com.app.ecommerce.entity.Order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class GetOrderByIdResponse {
	private Order order;
}
