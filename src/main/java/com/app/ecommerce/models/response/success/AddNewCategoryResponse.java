package com.app.ecommerce.models.response.success;

import com.app.ecommerce.models.request.CategoryRequestBody;

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

public class AddNewCategoryResponse {
	
	private Long id;
}
