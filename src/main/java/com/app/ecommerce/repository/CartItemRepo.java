package com.app.ecommerce.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.ecommerce.entity.CartItem;


@Repository
public interface CartItemRepo extends JpaRepository<CartItem,Long> {
	

}
