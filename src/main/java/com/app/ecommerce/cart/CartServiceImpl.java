package com.app.ecommerce.cart;

import com.app.ecommerce.auth.User;
import com.app.ecommerce.product.Product;
import com.app.ecommerce.product.ProductRepository;
import com.app.ecommerce.shared.constants.CacheConstants;
import com.app.ecommerce.shared.exception.CartNotOpenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CartResponse getCurrentCart(User owner) {
        log.info("getCurrentCart(owner={})", owner.getUsername());
        Cart cart = cartRepository.findByOwnerAndStatus(owner, CartStatus.OPEN)
                .orElseGet(() -> {
                    log.info("No OPEN cart found for user {}, creating new cart", owner.getUsername());
                    Cart newCart = Cart.builder()
                            .owner(owner)
                            .status(CartStatus.OPEN)
                            .cartItems(new HashSet<>())
                            .build();
                    return cartRepository.save(newCart);
                });
        return cartMapper.mapToResponse(cart);
    }

    @Override
    @CacheEvict(value = CacheConstants.CARTS, key = "#result.id")
    @Transactional
    public CartResponse addItem(User owner, AddCartItemRequest request) {
        log.info("addItem(owner={}, productId={}, quantity={})", owner.getUsername(), request.getProductId(), request.getQuantity());

        Cart cart = cartRepository.findByOwnerAndStatus(owner, CartStatus.OPEN)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .owner(owner)
                            .status(CartStatus.OPEN)
                            .cartItems(new HashSet<>())
                            .build();
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSuchElementException("Product with id " + request.getProductId() + " not found"));

        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setProductQuantity(existingItem.getProductQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .product(product)
                    .productQuantity(request.getQuantity())
                    .cart(cart)
                    .build();
            cartItemRepository.save(newItem);
            cart.getCartItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        log.info("Item added to cart {}", savedCart.getId());
        return cartMapper.mapToResponse(savedCart);
    }

    @Override
    @CacheEvict(value = CacheConstants.CARTS, key = "#result.id")
    @Transactional
    public CartResponse updateItemQuantity(User owner, UUID cartItemId, UpdateCartItemQuantityRequest request) {
        log.info("updateItemQuantity(owner={}, cartItemId={}, quantity={})", owner.getUsername(), cartItemId, request.getQuantity());

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NoSuchElementException("Cart item with id " + cartItemId + " not found"));

        if (!item.getCart().getOwner().getId().equals(owner.getId())) {
            throw new NoSuchElementException("Cart item with id " + cartItemId + " not found");
        }

        if (item.getCart().getStatus() != CartStatus.OPEN) {
            throw new CartNotOpenException();
        }

        Cart cart = item.getCart();

        if (request.getQuantity() == 0) {
            cart.getCartItems().remove(item);
            cartItemRepository.delete(item);
            log.info("Cart item {} removed (quantity set to 0)", cartItemId);
        } else {
            item.setProductQuantity(request.getQuantity());
            cartItemRepository.save(item);
            log.info("Cart item {} updated to quantity {}", cartItemId, request.getQuantity());
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.mapToResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(User owner, UUID cartItemId) {
        log.info("removeItem(owner={}, cartItemId={})", owner.getUsername(), cartItemId);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NoSuchElementException("Cart item with id " + cartItemId + " not found"));

        if (!item.getCart().getOwner().getId().equals(owner.getId())) {
            throw new NoSuchElementException("Cart item with id " + cartItemId + " not found");
        }

        if (item.getCart().getStatus() != CartStatus.OPEN) {
            throw new CartNotOpenException();
        }

        Cart cart = item.getCart();
        cart.getCartItems().remove(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);
        log.info("Cart item {} removed from cart {}", cartItemId, cart.getId());
        return cartMapper.mapToResponse(cart);
    }

}
