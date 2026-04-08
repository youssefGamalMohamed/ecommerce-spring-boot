package com.app.ecommerce.cart;

import com.app.ecommerce.auth.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;

    @Override
    @Transactional(readOnly = true)
    public CartItemResponse findById(UUID cartItemId, User owner) {
        log.info("findById({}, owner={})", cartItemId, owner.getUsername());
        CartItem cartItem = cartItemRepository.findByIdWithCartAndOwner(cartItemId)
                .orElseThrow(() -> new NoSuchElementException("CartItem with id " + cartItemId + " not found"));
        if (!cartItem.getCart().getOwner().getId().equals(owner.getId())) {
            throw new NoSuchElementException("CartItem with id " + cartItemId + " not found");
        }
        return cartItemMapper.mapToResponse(cartItem);
    }

}
