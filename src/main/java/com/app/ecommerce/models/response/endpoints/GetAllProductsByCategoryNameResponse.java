package com.app.ecommerce.models.response.endpoints;

import java.util.Set;

import com.app.ecommerce.entity.Product;

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

public class GetAllProductsByCategoryNameResponse {
	private Set<Product> products;
}
