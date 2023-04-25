package com.app.ecommerce.models.response.success;

import java.util.Set;

import com.app.ecommerce.entity.Category;
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

public class GetCategoryByIdResponse {
	private Category category;
}
