package com.app.ecommerce.service.framework;


import com.app.ecommerce.entity.Cart;


public interface ICartService {
	Cart createNewCart(Cart cartRequestBody);
}
