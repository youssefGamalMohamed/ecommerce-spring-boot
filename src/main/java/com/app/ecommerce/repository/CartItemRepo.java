package com.app.ecommerce.repository;



import com.app.ecommerce.entity.CartItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CartItemRepo extends JpaRepository<CartItem,UUID> {
	

}
