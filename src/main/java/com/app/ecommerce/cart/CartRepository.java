package com.app.ecommerce.cart;

import com.app.ecommerce.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByOwnerAndStatus(User owner, CartStatus status);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.product WHERE c.owner = :owner AND c.status = :status")
    Optional<Cart> findByOwnerAndStatusWithItems(@Param("owner") User owner, @Param("status") CartStatus status);

    boolean existsByOwnerAndStatus(User owner, CartStatus status);
}
