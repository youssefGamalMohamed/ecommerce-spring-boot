package com.app.ecommerce.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.ecommerce.entity.Order;

@Repository
public interface OrderRepo extends JpaRepository<Order,Long> {
	

}
