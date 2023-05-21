package com.app.ecommerce.models.request;

import java.util.Set;

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
public class PostCartRequestBody {

	@NotNull(message = "Cart Items Should Not Be Null or Empty")
	private Set<PostCartItemRequestBody> cartItems;
}
