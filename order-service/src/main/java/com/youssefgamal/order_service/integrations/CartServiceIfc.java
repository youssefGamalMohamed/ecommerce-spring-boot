package com.youssefgamal.order_service.integrations;

import com.youssefgamal.order_service.dtos.CartDto;

public interface CartServiceIfc {
	CartDto findById(Long id);
}
