package com.app.ecommerce.models.response.endpoints;

import java.util.List;

import com.app.ecommerce.entity.Category;

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

public class GetAllCategoriesResponse {
	
	private List<Category> categories;
}
