package com.app.ecommerce.models.response.endpoints;

import io.swagger.v3.oas.annotations.media.Schema;
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

public class DeleteCategoryResponse {

	@Schema(type = "string" , example = "Category Deleted Successfully")
	private String message;
}
