package com.app.ecommerce.service.framework;


import com.app.ecommerce.entity.Cart;
import com.app.ecommerce.models.request.PostCartRequestBody;


public interface ICartService {
	Cart createNewCart(PostCartRequestBody cartRequestBody);
}
