package com.app.ecommerce.models.response.success;

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

public class GetAllCategoriesReponse {
	
	private List<Category> categories;
}
