package com.app.ecommerce.cart;

import com.app.ecommerce.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByOwnerAndStatus(User owner, CartStatus status);

    boolean existsByOwnerAndStatus(User owner, CartStatus status);
}
