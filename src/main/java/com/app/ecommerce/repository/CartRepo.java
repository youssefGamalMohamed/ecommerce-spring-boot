package com.app.ecommerce.repository;



import com.app.ecommerce.entity.Cart;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CartRepo extends JpaRepository<Cart,UUID> {
	

}
