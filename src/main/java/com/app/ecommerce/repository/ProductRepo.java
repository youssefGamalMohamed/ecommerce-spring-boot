package com.app.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.ecommerce.entity.Product;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {
	
	List<Product> findByCategoriesId(Long categoryId);
	
}
