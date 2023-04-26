package com.app.ecommerce.service.framework;

import java.util.List;

import com.app.ecommerce.entity.Cart;
import com.app.ecommerce.models.request.PostCartRequestBody;


public interface ICartService {
	Cart createNewCart(PostCartRequestBody cartRequestBody);
}
