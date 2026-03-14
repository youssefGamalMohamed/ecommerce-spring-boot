package com.app.ecommerce.cart;

import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    @Override
    public Cart findById(UUID cartId) {
        log.info("findById({})", cartId);
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new NoSuchElementException("Cart with id " + cartId + " not found"));
    }

}
