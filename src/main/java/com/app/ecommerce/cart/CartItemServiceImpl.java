package com.app.ecommerce.cart;

import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;

    @Override
    public CartItem findById(UUID cartItemId) {
        log.info("findById({})", cartItemId);
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NoSuchElementException("CartItem with id " + cartItemId + " not found"));
    }

}
