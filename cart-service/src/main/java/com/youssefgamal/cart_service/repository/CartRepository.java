package com.youssefgamal.cart_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.youssefgamal.cart_service.entity.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long>{

}
