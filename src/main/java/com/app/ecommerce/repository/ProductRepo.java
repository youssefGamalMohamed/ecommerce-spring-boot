package com.app.ecommerce.repository;

import com.app.ecommerce.entity.Product;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {
	
	Set<Product> findByCategoriesId(Long categoryId);
	Set<Product> findByCategories_Name(String name);
	
}
