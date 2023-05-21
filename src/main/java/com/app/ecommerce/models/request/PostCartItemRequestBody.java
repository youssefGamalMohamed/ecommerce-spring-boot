package com.app.ecommerce.models.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class PostCartItemRequestBody {

	@NotNull(message = "Product Id Should Not Be Null in in Cart Item")
	private Long productId;

	@Min(1)
	private int quantity;
}
