package com.youssefgamal.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.youssefgamal.productservice.entity.Product;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {
	
	
	
}
