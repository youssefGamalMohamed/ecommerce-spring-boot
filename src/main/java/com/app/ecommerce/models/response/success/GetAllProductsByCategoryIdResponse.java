package com.app.ecommerce.models.response.success;

import java.util.List;
import java.util.Set;

import com.app.ecommerce.entity.Category;
import com.app.ecommerce.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class GetAllProductsByCategoryIdResponse {
	private List<Product> products;
}
