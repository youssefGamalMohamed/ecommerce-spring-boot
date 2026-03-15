package com.app.ecommerce.cart;

import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.app.ecommerce.shared.constants.CacheConstants;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;

    @Override
    @Cacheable(value = CacheConstants.CARTS, key = "#cartId")
    public CartDto findById(UUID cartId) {
        log.info("findById({})", cartId);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NoSuchElementException("Cart with id " + cartId + " not found"));
        return cartMapper.mapToDto(cart);
    }

}
