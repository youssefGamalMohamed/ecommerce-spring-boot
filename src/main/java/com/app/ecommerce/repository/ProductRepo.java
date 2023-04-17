package com.app.ecommerce.repository;

import com.app.ecommerce.entity.Product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {
	
	List<Product> findByCategoriesId(Long categoryId);
}
