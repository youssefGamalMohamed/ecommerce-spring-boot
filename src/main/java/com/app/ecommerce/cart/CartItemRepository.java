package com.app.ecommerce.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.cart c JOIN FETCH c.owner WHERE ci.id = :id")
    Optional<CartItem> findByIdWithCartAndOwner(@Param("id") UUID id);

}
